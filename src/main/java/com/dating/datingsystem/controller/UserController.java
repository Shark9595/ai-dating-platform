package com.dating.datingsystem.controller;

import com.dating.datingsystem.common.Result;
import com.dating.datingsystem.dto.UserDTO;
import com.dating.datingsystem.entity.MatchPreference;
import com.dating.datingsystem.entity.UserProfile;
import com.dating.datingsystem.service.UserService;
import com.dating.datingsystem.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private SecurityUtil securityUtil;

    @GetMapping("/info")
    public Result<UserDTO> getUserInfo() {
        Long userId = securityUtil.getCurrentUserId();
        UserDTO user = userService.getUserInfo(userId);
        return Result.success(user);
    }

    @GetMapping("/profile")
    public Result<UserProfile> getUserProfile() {
        Long userId = securityUtil.getCurrentUserId();
        UserProfile profile = userService.getUserProfile(userId);
        return Result.success(profile);
    }

    @PutMapping("/profile")
    public Result<UserProfile> updateUserProfile(@RequestBody UserProfile profile) {
        Long userId = securityUtil.getCurrentUserId();
        UserProfile updated = userService.updateUserProfile(userId, profile);
        return Result.success("资料更新成功", updated);
    }

    @GetMapping("/preference")
    public Result<MatchPreference> getMatchPreference() {
        Long userId = securityUtil.getCurrentUserId();
        MatchPreference preference = userService.getMatchPreference(userId);
        return Result.success(preference);
    }

    @PutMapping("/preference")
    public Result<MatchPreference> updateMatchPreference(@RequestBody MatchPreference preference) {
        Long userId = securityUtil.getCurrentUserId();
        MatchPreference updated = userService.updateMatchPreference(userId, preference);
        return Result.success("择偶偏好更新成功", updated);
    }

    @PostMapping("/real-name")
    public Result<Void> submitRealName(@RequestBody Map<String, String> params) {
        Long userId = securityUtil.getCurrentUserId();
        userService.submitRealName(userId, params.get("realName"), params.get("idCard"));
        return Result.success("实名认证提交成功");
    }

    @PostMapping("/change-password")
    public Result<Void> changePassword(@RequestBody Map<String, String> params) {
        Long userId = securityUtil.getCurrentUserId();
        userService.changePassword(userId, params.get("oldPassword"), params.get("newPassword"));
        return Result.success("密码修改成功");
    }

    @PutMapping("/avatar")
    public Result<Void> updateAvatar(@RequestBody Map<String, String> params) {
        Long userId = securityUtil.getCurrentUserId();
        userService.updateAvatar(userId, params.get("avatar"));
        return Result.success("头像更新成功");
    }

    @PutMapping("/nickname")
    public Result<Void> updateNickname(@RequestBody Map<String, String> params) {
        Long userId = securityUtil.getCurrentUserId();
        userService.updateNickname(userId, params.get("nickname"));
        return Result.success("昵称更新成功");
    }

    @GetMapping("/{id}")
    public Result<UserProfile> getUserProfileById(@PathVariable Long id) {
        UserProfile profile = userService.getUserProfile(id);
        return Result.success(profile);
    }
}
