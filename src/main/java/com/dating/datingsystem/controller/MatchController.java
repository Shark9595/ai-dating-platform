package com.dating.datingsystem.controller;

import com.dating.datingsystem.common.Result;
import com.dating.datingsystem.dto.UserDTO;
import com.dating.datingsystem.entity.User;
import com.dating.datingsystem.entity.UserProfile;
import com.dating.datingsystem.repository.UserProfileRepository;
import com.dating.datingsystem.repository.UserRepository;
import com.dating.datingsystem.service.DeepSeekService;
import com.dating.datingsystem.service.MatchService;
import com.dating.datingsystem.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/match")
public class MatchController {

    @Autowired
    private MatchService matchService;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private DeepSeekService deepSeekService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @GetMapping("/recommendations")
    public Result<List<Map<String, Object>>> getRecommendations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = securityUtil.getCurrentUserId();
        List<Map<String, Object>> list = matchService.getMatchRecommendations(userId, page, size);
        return Result.success(list);
    }

    @GetMapping("/daily")
    public Result<List<UserDTO>> getDailyRecommendations(
            @RequestParam(defaultValue = "5") int count) {
        Long userId = securityUtil.getCurrentUserId();
        List<UserDTO> list = matchService.getDailyRecommendations(userId, count);
        return Result.success(list);
    }

    @PostMapping("/search")
    public Result<List<Map<String, Object>>> searchUsers(
            @RequestBody Map<String, Object> params,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = securityUtil.getCurrentUserId();
        List<Map<String, Object>> list = matchService.searchUsers(userId, params, page, size);
        return Result.success(list);
    }

    @PostMapping("/like/{targetUserId}")
    public Result<Void> likeUser(@PathVariable Long targetUserId) {
        Long userId = securityUtil.getCurrentUserId();
        matchService.likeUser(userId, targetUserId);
        return Result.success("喜欢成功");
    }

    @PostMapping("/unlike/{targetUserId}")
    public Result<Void> unlikeUser(@PathVariable Long targetUserId) {
        Long userId = securityUtil.getCurrentUserId();
        matchService.unlikeUser(userId, targetUserId);
        return Result.success("取消喜欢成功");
    }

    @PostMapping("/dislike/{targetUserId}")
    public Result<Void> dislikeUser(@PathVariable Long targetUserId) {
        Long userId = securityUtil.getCurrentUserId();
        matchService.dislikeUser(userId, targetUserId);
        return Result.success("操作成功");
    }

    @PostMapping("/favorite/{targetUserId}")
    public Result<Void> favoriteUser(@PathVariable Long targetUserId) {
        Long userId = securityUtil.getCurrentUserId();
        matchService.favoriteUser(userId, targetUserId);
        return Result.success("收藏成功");
    }

    @PostMapping("/unfavorite/{targetUserId}")
    public Result<Void> unfavoriteUser(@PathVariable Long targetUserId) {
        Long userId = securityUtil.getCurrentUserId();
        matchService.unfavoriteUser(userId, targetUserId);
        return Result.success("取消收藏成功");
    }

    @GetMapping("/liked")
    public Result<List<UserDTO>> getLikedUsers() {
        Long userId = securityUtil.getCurrentUserId();
        List<UserDTO> list = matchService.getLikedUsers(userId);
        return Result.success(list);
    }

    @GetMapping("/favorites")
    public Result<List<UserDTO>> getFavoriteUsers() {
        Long userId = securityUtil.getCurrentUserId();
        List<UserDTO> list = matchService.getFavoriteUsers(userId);
        return Result.success(list);
    }

    @GetMapping("/fans")
    public Result<List<UserDTO>> getFans() {
        Long userId = securityUtil.getCurrentUserId();
        List<UserDTO> list = matchService.getFans(userId);
        return Result.success(list);
    }

    @GetMapping("/is-matched/{targetUserId}")
    public Result<Boolean> isMatched(@PathVariable Long targetUserId) {
        Long userId = securityUtil.getCurrentUserId();
        boolean matched = matchService.isMatched(userId, targetUserId);
        return Result.success(matched);
    }

    @PostMapping("/visit/{visitedId}")
    public Result<Void> recordVisitor(
            @PathVariable Long visitedId,
            @RequestParam(defaultValue = "false") boolean anonymous) {
        Long userId = securityUtil.getCurrentUserId();
        matchService.recordVisitor(userId, visitedId, anonymous);
        return Result.success();
    }

    @GetMapping("/visitors")
    public Result<List<Map<String, Object>>> getVisitorRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = securityUtil.getCurrentUserId();
        List<Map<String, Object>> list = matchService.getVisitorRecords(userId, page, size);
        return Result.success(list);
    }

    @GetMapping("/visitor-count")
    public Result<Long> getVisitorCount() {
        Long userId = securityUtil.getCurrentUserId();
        long count = matchService.getVisitorCount(userId);
        return Result.success(count);
    }

    /**
     * AI 智能聊天
     */
    @PostMapping("/ai/chat")
    public Result<Map<String, Object>> aiChat(@RequestBody Map<String, Object> params) {
        Long userId = securityUtil.getCurrentUserId();
        String message = (String) params.get("message");

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 构建用户信息
        StringBuilder userInfo = new StringBuilder();
        userInfo.append("昵称：").append(user.getNickname());
        if (user.getAge() != null) userInfo.append("，年龄：").append(user.getAge()).append("岁");
        UserProfile profile = userProfileRepository.findByUserId(userId).orElse(null);
        if (profile != null) {
            if (profile.getOccupation() != null) userInfo.append("，职业：").append(profile.getOccupation());
            if (profile.getHobbies() != null) userInfo.append("，爱好：").append(profile.getHobbies());
        }

        String reply = deepSeekService.getChatReply(message, userInfo.toString());

        Map<String, Object> result = new HashMap<>();
        result.put("reply", reply);
        return Result.success(result);
    }

    /**
     * AI 生成约会建议
     */
    @GetMapping("/ai/date-suggestion/{targetUserId}")
    public Result<Map<String, Object>> getDateSuggestion(@PathVariable Long targetUserId) {
        Long userId = securityUtil.getCurrentUserId();

        User user1 = userRepository.findById(userId).orElse(null);
        User user2 = userRepository.findById(targetUserId).orElse(null);

        if (user1 == null || user2 == null) {
            return Result.error("用户不存在");
        }

        Map<String, Object> info1 = buildUserInfoMap(user1);
        Map<String, Object> info2 = buildUserInfoMap(user2);

        String suggestion = deepSeekService.getDateSuggestion(info1, info2);

        Map<String, Object> result = new HashMap<>();
        result.put("suggestion", suggestion);
        return Result.success(result);
    }

    /**
     * AI 生成聊天话题
     */
    @GetMapping("/ai/chat-topics/{targetUserId}")
    public Result<Map<String, Object>> getChatTopics(@PathVariable Long targetUserId) {
        Long userId = securityUtil.getCurrentUserId();

        User user1 = userRepository.findById(userId).orElse(null);
        User user2 = userRepository.findById(targetUserId).orElse(null);

        if (user1 == null || user2 == null) {
            return Result.error("用户不存在");
        }

        Map<String, Object> info1 = buildUserInfoMap(user1);
        Map<String, Object> info2 = buildUserInfoMap(user2);

        List<String> topics = deepSeekService.generateChatTopics(info1, info2);

        Map<String, Object> result = new HashMap<>();
        result.put("topics", topics);
        return Result.success(result);
    }

    private Map<String, Object> buildUserInfoMap(User user) {
        Map<String, Object> info = new HashMap<>();
        info.put("nickname", user.getNickname());
        info.put("age", user.getAge() != null ? user.getAge() : 25);
        info.put("occupation", "未知");
        info.put("hobbies", "未知");

        UserProfile profile = userProfileRepository.findByUserId(user.getId()).orElse(null);
        if (profile != null) {
            if (profile.getOccupation() != null) info.put("occupation", profile.getOccupation());
            if (profile.getHobbies() != null) info.put("hobbies", profile.getHobbies());
        }
        return info;
    }
}
