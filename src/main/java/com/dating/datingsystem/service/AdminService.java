package com.dating.datingsystem.service;

import com.dating.datingsystem.dto.UserDTO;
import com.dating.datingsystem.entity.*;
import com.dating.datingsystem.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private VipPackageRepository vipPackageRepository;

    @Autowired
    private OfflineActivityRepository activityRepository;

    @Autowired
    private SysConfigRepository sysConfigRepository;

    @Autowired
    private OperationLogRepository operationLogRepository;

    @Autowired
    private SensitiveWordRepository sensitiveWordRepository;

    @Autowired
    private DailyTaskRepository dailyTaskRepository;

    @Autowired
    private VirtualGiftRepository virtualGiftRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Map<String, Object> getUserList(int page, int size, String keyword, Integer status, String role) {
        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("deleted"), 0));

            if (keyword != null && !keyword.isEmpty()) {
                predicates.add(cb.or(
                        cb.like(root.get("username"), "%" + keyword + "%"),
                        cb.like(root.get("nickname"), "%" + keyword + "%"),
                        cb.like(root.get("phone"), "%" + keyword + "%")
                ));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (role != null && !role.isEmpty()) {
                predicates.add(cb.equal(root.get("role"), role));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<User> userPage = userRepository.findAll(spec, PageRequest.of(page, size));
        Map<String, Object> result = new HashMap<>();
        result.put("total", userPage.getTotalElements());
        result.put("list", userPage.getContent());
        result.put("pageNum", page);
        result.put("pageSize", size);
        return result;
    }

    @Transactional
    public void updateUserStatus(Long userId, Integer status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setStatus(status);
        userRepository.save(user);
    }

    @Transactional
    public void updateUserRole(Long userId, String role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setRole(role);
        userRepository.save(user);
    }

    @Transactional
    public void updateUserVip(Long userId, Integer vipLevel, Integer days) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireTime = user.getVipExpireTime();
        if (expireTime == null || expireTime.isBefore(now)) {
            expireTime = now.plusDays(days);
        } else {
            expireTime = expireTime.plusDays(days);
        }

        user.setVipLevel(vipLevel);
        user.setVipExpireTime(expireTime);
        userRepository.save(user);
    }

    @Transactional
    public User createUser(User user, String password) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        user.setPassword(passwordEncoder.encode(password));
        return userRepository.save(user);
    }

    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        long totalUsers = userRepository.count();
        long todayUsers = userRepository.findAll().stream()
                .filter(u -> u.getCreateTime() != null
                        && u.getCreateTime().toLocalDate().equals(LocalDate.now()))
                .count();

        long totalOrders = orderRepository.count();
        BigDecimal totalAmount = orderRepository.findAll().stream()
                .filter(o -> o.getPayStatus() != null && o.getPayStatus() == 1)
                .map(o -> o.getPayAmount() != null ? o.getPayAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalPosts = postRepository.count();
        long activePosts = postRepository.findByStatusAndAuditStatusOrderByCreateTimeDesc(1, 1).size();

        stats.put("totalUsers", totalUsers);
        stats.put("todayUsers", todayUsers);
        stats.put("totalOrders", totalOrders);
        stats.put("totalAmount", totalAmount);
        stats.put("totalPosts", totalPosts);
        stats.put("activePosts", activePosts);

        return stats;
    }

    public List<VipPackage> getAllVipPackages() {
        return vipPackageRepository.findAll();
    }

    @Transactional
    public VipPackage saveVipPackage(VipPackage vipPackage) {
        return vipPackageRepository.save(vipPackage);
    }

    @Transactional
    public void deleteVipPackage(Long id) {
        vipPackageRepository.deleteById(id);
    }

    public List<OfflineActivity> getAllActivities() {
        return activityRepository.findAll();
    }

    @Transactional
    public OfflineActivity saveActivity(OfflineActivity activity) {
        return activityRepository.save(activity);
    }

    @Transactional
    public void deleteActivity(Long id) {
        activityRepository.deleteById(id);
    }

    public List<SysConfig> getAllConfigs() {
        return sysConfigRepository.findAll();
    }

    @Transactional
    public SysConfig saveConfig(SysConfig config) {
        return sysConfigRepository.save(config);
    }

    public String getConfigValue(String key) {
        return sysConfigRepository.findByConfigKey(key)
                .map(SysConfig::getConfigValue)
                .orElse(null);
    }

    public Map<String, Object> getOperationLogs(int page, int size) {
        Page<OperationLog> logPage = operationLogRepository.findAll(PageRequest.of(page, size));
        Map<String, Object> result = new HashMap<>();
        result.put("total", logPage.getTotalElements());
        result.put("list", logPage.getContent());
        return result;
    }

    public List<SensitiveWord> getAllSensitiveWords() {
        return sensitiveWordRepository.findAll();
    }

    @Transactional
    public SensitiveWord saveSensitiveWord(SensitiveWord word) {
        return sensitiveWordRepository.save(word);
    }

    @Transactional
    public void deleteSensitiveWord(Long id) {
        sensitiveWordRepository.deleteById(id);
    }

    public List<DailyTask> getAllDailyTasks() {
        return dailyTaskRepository.findAll();
    }

    @Transactional
    public DailyTask saveDailyTask(DailyTask task) {
        return dailyTaskRepository.save(task);
    }

    @Transactional
    public void deleteDailyTask(Long id) {
        dailyTaskRepository.deleteById(id);
    }

    public List<VirtualGift> getAllVirtualGifts() {
        return virtualGiftRepository.findAll();
    }

    @Transactional
    public VirtualGift saveVirtualGift(VirtualGift gift) {
        return virtualGiftRepository.save(gift);
    }

    @Transactional
    public void deleteVirtualGift(Long id) {
        virtualGiftRepository.deleteById(id);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setDeleted(1);
        userRepository.save(user);
    }

    public UserDTO getUserDetail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return convertToDTO(user);
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setAvatar(user.getAvatar());
        dto.setGender(user.getGender());
        dto.setAge(user.getAge());
        dto.setRole(user.getRole());
        dto.setVipLevel(user.getVipLevel());
        dto.setPoints(user.getPoints());
        dto.setRealNameStatus(user.getRealNameStatus());
        dto.setStatus(user.getStatus());
        return dto;
    }
}
