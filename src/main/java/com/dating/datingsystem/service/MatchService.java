package com.dating.datingsystem.service;

import com.dating.datingsystem.dto.UserDTO;
import com.dating.datingsystem.entity.*;
import com.dating.datingsystem.repository.*;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MatchService {

    private static final Logger logger = LoggerFactory.getLogger(MatchService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private MatchPreferenceRepository matchPreferenceRepository;

    @Autowired
    private MatchRecordRepository matchRecordRepository;

    @Autowired
    private VisitorRecordRepository visitorRecordRepository;

    @Autowired
    private ChatSessionRepository chatSessionRepository;

    @Autowired
    private DeepSeekService deepSeekService;

    @CircuitBreaker(name = "match", fallbackMethod = "getMatchRecommendationsFallback")
    @RateLimiter(name = "match", fallbackMethod = "getMatchRecommendationsRateLimitFallback")
    public List<Map<String, Object>> getMatchRecommendations(Long userId, int page, int size) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        MatchPreference preference = matchPreferenceRepository.findByUserId(userId).orElse(null);

        List<User> allUsers = userRepository.findAll();
        List<Map<String, Object>> recommendations = new ArrayList<>();

        for (User user : allUsers) {
            if (user.getId().equals(userId) || user.getStatus() != 1 || user.getDeleted() == 1) {
                continue;
            }
            if (currentUser.getGender() != null && user.getGender() != null
                    && currentUser.getGender().equals(user.getGender())) {
                continue;
            }

            // 使用简单的匹配分数计算
            BigDecimal score = calculateSimpleMatchScore(currentUser, user);
            if (score.compareTo(BigDecimal.ZERO) > 0) {
                Map<String, Object> item = new HashMap<>();
                item.put("user", convertToDTO(user));
                item.put("score", score);
                item.put("profile", userProfileRepository.findByUserId(user.getId()).orElse(null));

                // 尝试使用 DeepSeek AI 生成推荐理由
                try {
                    Map<String, Object> user1Info = getUserInfoMap(currentUser);
                    Map<String, Object> user2Info = getUserInfoMap(user);
                    String reason = deepSeekService.generateMatchReason(user1Info, user2Info, score.doubleValue());
                    item.put("reason", reason);
                } catch (Exception e) {
                    // 如果AI调用失败，使用本地推荐理由
                    logger.warn("DeepSeek API 调用失败，使用本地推荐理由: {}", e.getMessage());
                    item.put("reason", generateLocalReason(currentUser, user, score));
                }

                recommendations.add(item);
            }
        }

        recommendations.sort((a, b) -> ((BigDecimal) b.get("score")).compareTo((BigDecimal) a.get("score")));

        int start = page * size;
        int end = Math.min(start + size, recommendations.size());
        if (start >= recommendations.size()) {
            return new ArrayList<>();
        }
        return recommendations.subList(start, end);
    }

    /**
     * 生成本地推荐理由（不依赖外部AI，作为降级方案）
     */
    private String generateLocalReason(User currentUser, User targetUser, BigDecimal score) {
        String[] reasons = {
                "你们都是热爱生活的人，共同话题很多哦！",
                "性格互补，他/她的稳重能给你安全感",
                "相似的价值观和生活态度，相处会更融洽",
                "都向往温馨的家庭生活，未来规划一致",
                "你们的兴趣爱好有交集，聊天不会冷场",
                "年龄相近，生活节奏相似，沟通更顺畅",
                "地理位置相近，方便约会见面",
                "都是认真对待感情的人，值得认识"
        };
        
        // 根据匹配度选择不同的推荐理由
        int index = score.intValue() / 10 % reasons.length;
        return reasons[index];
    }

    private BigDecimal calculateMatchScore(User currentUser, User targetUser, MatchPreference preference) {
        BigDecimal score = new BigDecimal("50");

        UserProfile currentProfile = userProfileRepository.findByUserId(currentUser.getId()).orElse(null);
        UserProfile targetProfile = userProfileRepository.findByUserId(targetUser.getId()).orElse(null);

        if (preference != null && targetUser.getAge() != null) {
            if (preference.getMinAge() != null && targetUser.getAge() < preference.getMinAge()) {
                return BigDecimal.ZERO;
            }
            if (preference.getMaxAge() != null && targetUser.getAge() > preference.getMaxAge()) {
                return BigDecimal.ZERO;
            }
            score = score.add(new BigDecimal("10"));
        }

        if (preference != null && targetProfile != null) {
            if (preference.getCity() != null && !preference.getCity().isEmpty()
                    && preference.getCity().equals(targetProfile.getCity())) {
                score = score.add(new BigDecimal("15"));
            }

            if (preference.getEducation() != null && !preference.getEducation().isEmpty()
                    && preference.getEducation().equals(targetProfile.getEducation())) {
                score = score.add(new BigDecimal("10"));
            }

            if (preference.getMinHeight() != null && targetProfile.getHeight() != null
                    && targetProfile.getHeight() >= preference.getMinHeight()) {
                score = score.add(new BigDecimal("5"));
            }
            if (preference.getMaxHeight() != null && targetProfile.getHeight() != null
                    && targetProfile.getHeight() <= preference.getMaxHeight()) {
                score = score.add(new BigDecimal("5"));
            }

            if (preference.getMinSalary() != null && targetProfile.getSalary() != null
                    && targetProfile.getSalary().compareTo(preference.getMinSalary()) >= 0) {
                score = score.add(new BigDecimal("10"));
            }
        }

        if (targetUser.getVipLevel() != null && targetUser.getVipLevel() > 0) {
            score = score.add(new BigDecimal("5"));
        }

        if (targetUser.getRealNameStatus() != null && targetUser.getRealNameStatus() == 1) {
            score = score.add(new BigDecimal("5"));
        }

        return score.min(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateSimpleMatchScore(User currentUser, User targetUser) {
        BigDecimal score = new BigDecimal("50");

        UserProfile currentProfile = userProfileRepository.findByUserId(currentUser.getId()).orElse(null);
        UserProfile targetProfile = userProfileRepository.findByUserId(targetUser.getId()).orElse(null);

        // 年龄相似度
        if (currentUser.getAge() != null && targetUser.getAge() != null) {
            int ageDiff = Math.abs(currentUser.getAge() - targetUser.getAge());
            if (ageDiff <= 3) {
                score = score.add(new BigDecimal("20"));
            } else if (ageDiff <= 5) {
                score = score.add(new BigDecimal("10"));
            }
        }

        // 地区匹配
        if (currentProfile != null && targetProfile != null
                && currentProfile.getCity() != null && targetProfile.getCity() != null
                && currentProfile.getCity().equals(targetProfile.getCity())) {
            score = score.add(new BigDecimal("15"));
        }

        // 学历匹配
        if (currentProfile != null && targetProfile != null
                && currentProfile.getEducation() != null && targetProfile.getEducation() != null
                && currentProfile.getEducation().equals(targetProfile.getEducation())) {
            score = score.add(new BigDecimal("10"));
        }

        // VIP加分
        if (targetUser.getVipLevel() != null && targetUser.getVipLevel() > 0) {
            score = score.add(new BigDecimal("5"));
        }

        return score.min(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP);
    }

    public List<Map<String, Object>> searchUsers(Long userId, Map<String, Object> params, int page, int size) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), 1));
            predicates.add(cb.equal(root.get("deleted"), 0));
            predicates.add(cb.notEqual(root.get("id"), userId));

            if (currentUser.getGender() != null) {
                predicates.add(cb.notEqual(root.get("gender"), currentUser.getGender()));
            }

            if (params.containsKey("minAge") && params.get("minAge") != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("age"), (Integer) params.get("minAge")));
            }
            if (params.containsKey("maxAge") && params.get("maxAge") != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("age"), (Integer) params.get("maxAge")));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<User> userPage = userRepository.findAll(spec, PageRequest.of(page, size));
        List<Map<String, Object>> result = new ArrayList<>();
        for (User user : userPage.getContent()) {
            Map<String, Object> item = new HashMap<>();
            item.put("user", convertToDTO(user));
            item.put("profile", userProfileRepository.findByUserId(user.getId()).orElse(null));
            
            // 计算匹配分数
            BigDecimal score = calculateSimpleMatchScore(currentUser, user);
            item.put("score", score);
            
            // 使用本地生成推荐理由
            String reason = generateLocalReason(currentUser, user, score);
            item.put("reason", reason);
            
            result.add(item);
        }
        return result;
    }

    @Transactional
    public void likeUser(Long userId, Long targetUserId) {
        MatchRecord record = matchRecordRepository
                .findByUserIdAndTargetUserIdAndActionType(userId, targetUserId, 1)
                .orElse(null);

        if (record == null) {
            record = new MatchRecord();
            record.setUserId(userId);
            record.setTargetUserId(targetUserId);
            record.setActionType(1);
            matchRecordRepository.save(record);
        }

        MatchRecord backRecord = matchRecordRepository
                .findByUserIdAndTargetUserIdAndActionType(targetUserId, userId, 1)
                .orElse(null);

        if (backRecord != null) {
            createChatSession(userId, targetUserId);
        }
    }

    @Transactional
    public void unlikeUser(Long userId, Long targetUserId) {
        matchRecordRepository
                .findByUserIdAndTargetUserIdAndActionType(userId, targetUserId, 1)
                .ifPresent(matchRecordRepository::delete);
    }

    @Transactional
    public void dislikeUser(Long userId, Long targetUserId) {
        MatchRecord record = matchRecordRepository
                .findByUserIdAndTargetUserIdAndActionType(userId, targetUserId, 2)
                .orElse(null);

        if (record == null) {
            record = new MatchRecord();
            record.setUserId(userId);
            record.setTargetUserId(targetUserId);
            record.setActionType(2);
            matchRecordRepository.save(record);
        }
    }

    @Transactional
    public void favoriteUser(Long userId, Long targetUserId) {
        MatchRecord record = matchRecordRepository
                .findByUserIdAndTargetUserIdAndActionType(userId, targetUserId, 3)
                .orElse(null);

        if (record == null) {
            record = new MatchRecord();
            record.setUserId(userId);
            record.setTargetUserId(targetUserId);
            record.setActionType(3);
            matchRecordRepository.save(record);
        }
    }

    @Transactional
    public void unfavoriteUser(Long userId, Long targetUserId) {
        matchRecordRepository
                .findByUserIdAndTargetUserIdAndActionType(userId, targetUserId, 3)
                .ifPresent(matchRecordRepository::delete);
    }

    public List<UserDTO> getLikedUsers(Long userId) {
        List<MatchRecord> records = matchRecordRepository.findByUserIdAndActionType(userId, 1);
        return records.stream()
                .map(record -> userRepository.findById(record.getTargetUserId()).orElse(null))
                .filter(Objects::nonNull)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<UserDTO> getFavoriteUsers(Long userId) {
        List<MatchRecord> records = matchRecordRepository.findByUserIdAndActionType(userId, 3);
        return records.stream()
                .map(record -> userRepository.findById(record.getTargetUserId()).orElse(null))
                .filter(Objects::nonNull)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<UserDTO> getFans(Long userId) {
        List<User> users = userRepository.findAll();
        List<UserDTO> fans = new ArrayList<>();
        for (User user : users) {
            if (matchRecordRepository.existsByUserIdAndTargetUserIdAndActionType(user.getId(), userId, 1)) {
                if (!matchRecordRepository.existsByUserIdAndTargetUserIdAndActionType(userId, user.getId(), 1)) {
                    fans.add(convertToDTO(user));
                }
            }
        }
        return fans;
    }

    public boolean isMatched(Long userId, Long targetUserId) {
        boolean userLiked = matchRecordRepository.existsByUserIdAndTargetUserIdAndActionType(userId, targetUserId, 1);
        boolean targetLiked = matchRecordRepository.existsByUserIdAndTargetUserIdAndActionType(targetUserId, userId, 1);
        return userLiked && targetLiked;
    }

    @Transactional
    public void recordVisitor(Long visitorId, Long visitedId, boolean isAnonymous) {
        VisitorRecord record = new VisitorRecord();
        record.setVisitorId(visitorId);
        record.setVisitedId(visitedId);
        record.setIsAnonymous(isAnonymous ? 1 : 0);
        visitorRecordRepository.save(record);
    }

    public List<Map<String, Object>> getVisitorRecords(Long userId, int page, int size) {
        List<VisitorRecord> records = visitorRecordRepository.findByVisitedIdAndIsAnonymousOrderByVisitTimeDesc(userId, 0);
        int start = page * size;
        int end = Math.min(start + size, records.size());
        if (start >= records.size()) {
            return new ArrayList<>();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (VisitorRecord record : records.subList(start, end)) {
            Map<String, Object> item = new HashMap<>();
            item.put("record", record);
            item.put("visitor", userRepository.findById(record.getVisitorId())
                    .map(this::convertToDTO).orElse(null));
            result.add(item);
        }
        return result;
    }

    public long getVisitorCount(Long userId) {
        return visitorRecordRepository.countByVisitedId(userId);
    }

    public List<UserDTO> getDailyRecommendations(Long userId, int count) {
        List<Map<String, Object>> recommendations = getMatchRecommendations(userId, 0, count);
        return recommendations.stream()
                .map(item -> (UserDTO) item.get("user"))
                .collect(Collectors.toList());
    }

    private void createChatSession(Long user1Id, Long user2Id) {
        ChatSession session = chatSessionRepository.findByUser1IdAndUser2Id(user1Id, user2Id)
                .orElse(null);
        if (session == null) {
            session = chatSessionRepository.findByUser1IdAndUser2Id(user2Id, user1Id).orElse(null);
        }
        if (session == null) {
            session = new ChatSession();
            session.setUser1Id(user1Id);
            session.setUser2Id(user2Id);
            chatSessionRepository.save(session);
        }
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setAvatar(user.getAvatar());
        dto.setGender(user.getGender());
        dto.setAge(user.getAge());
        dto.setVipLevel(user.getVipLevel());
        dto.setRealNameStatus(user.getRealNameStatus());
        dto.setStatus(user.getStatus());
        return dto;
    }

    private Map<String, Object> getUserInfoMap(User user) {
        Map<String, Object> info = new HashMap<>();
        info.put("id", user.getId());
        info.put("nickname", user.getNickname());
        info.put("age", user.getAge() != null ? user.getAge() : 25);
        info.put("gender", user.getGender());
        info.put("occupation", "未知");
        info.put("education", "未知");
        info.put("hobbies", "未知");

        UserProfile profile = userProfileRepository.findByUserId(user.getId()).orElse(null);
        if (profile != null) {
            if (profile.getOccupation() != null) info.put("occupation", profile.getOccupation());
            if (profile.getEducation() != null) info.put("education", profile.getEducation());
            if (profile.getHobbies() != null) info.put("hobbies", profile.getHobbies());
        }
        return info;
    }

    /**
     * 匹配推荐降级方法 - 熔断器打开时返回本地推荐
     */
    private List<Map<String, Object>> getMatchRecommendationsFallback(Long userId, int page, int size, Exception e) {
        logger.warn("匹配服务熔断降级: userId={}, error={}", userId, e.getMessage());

        // 返回本地缓存的简单推荐（基于基础匹配逻辑，不调用AI）
        List<Map<String, Object>> fallbackRecommendations = new ArrayList<>();

        try {
            User currentUser = userRepository.findById(userId).orElse(null);
            if (currentUser == null) {
                return fallbackRecommendations;
            }

            List<User> allUsers = userRepository.findAll();
            int count = 0;
            int startIndex = page * size;

            for (User user : allUsers) {
                if (count >= startIndex + size) break;
                if (user.getId().equals(userId) || user.getStatus() != 1 || user.getDeleted() == 1) continue;
                if (currentUser.getGender() != null && user.getGender() != null
                        && currentUser.getGender().equals(user.getGender())) continue;

                if (count >= startIndex) {
                    BigDecimal simpleScore = calculateSimpleMatchScore(currentUser, user);
                    if (simpleScore.compareTo(BigDecimal.ZERO) > 0) {
                        Map<String, Object> item = new HashMap<>();
                        item.put("user", convertToDTO(user));
                        item.put("score", simpleScore);
                        item.put("profile", userProfileRepository.findByUserId(user.getId()).orElse(null));
                        item.put("reason", generateLocalReason(currentUser, user, simpleScore));
                        fallbackRecommendations.add(item);
                    }
                }
                count++;
            }
        } catch (Exception ex) {
            logger.error("生成降级推荐失败: {}", ex.getMessage());
        }

        return fallbackRecommendations;
    }

    /**
     * 匹配推荐限流降级方法 - 限流时也返回本地推荐
     */
    private List<Map<String, Object>> getMatchRecommendationsRateLimitFallback(Long userId, int page, int size, Exception e) {
        logger.warn("匹配服务触发限流: userId={}", userId);
        // 限流时也返回本地推荐，保证功能可用
        return getMatchRecommendationsFallback(userId, page, size, e);
    }
}
