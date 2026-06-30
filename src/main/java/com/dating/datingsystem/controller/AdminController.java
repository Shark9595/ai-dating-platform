package com.dating.datingsystem.controller;

import com.dating.datingsystem.common.Result;
import com.dating.datingsystem.dto.UserDTO;
import com.dating.datingsystem.entity.*;
import com.dating.datingsystem.service.AdminService;
import com.dating.datingsystem.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private SecurityUtil securityUtil;

    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = adminService.getStatistics();
        return Result.success(stats);
    }

    @GetMapping("/users")
    public Result<Map<String, Object>> getUserList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String role) {
        Map<String, Object> result = adminService.getUserList(page, size, keyword, status, role);
        return Result.success(result);
    }

    @GetMapping("/user/{userId}")
    public Result<UserDTO> getUserDetail(@PathVariable Long userId) {
        UserDTO user = adminService.getUserDetail(userId);
        return Result.success(user);
    }

    @PostMapping("/user/create")
    public Result<User> createUser(@RequestBody Map<String, Object> params) {
        User user = new User();
        user.setUsername(params.get("username").toString());
        user.setNickname(params.get("nickname") != null ? params.get("nickname").toString() : params.get("username").toString());
        user.setPhone(params.get("phone") != null ? params.get("phone").toString() : null);
        user.setRole(params.get("role") != null ? params.get("role").toString() : "USER");
        user.setGender(params.get("gender") != null ? Integer.valueOf(params.get("gender").toString()) : null);
        user.setAge(params.get("age") != null ? Integer.valueOf(params.get("age").toString()) : null);
        String password = params.get("password") != null ? params.get("password").toString() : "123456";
        User saved = adminService.createUser(user, password);
        return Result.success("创建成功", saved);
    }

    @PutMapping("/user/status/{userId}")
    public Result<Void> updateUserStatus(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> params) {
        Integer status = Integer.valueOf(params.get("status").toString());
        adminService.updateUserStatus(userId, status);
        return Result.success("状态更新成功");
    }

    @PutMapping("/user/role/{userId}")
    public Result<Void> updateUserRole(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> params) {
        String role = params.get("role").toString();
        adminService.updateUserRole(userId, role);
        return Result.success("角色更新成功");
    }

    @PutMapping("/user/vip/{userId}")
    public Result<Void> updateUserVip(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> params) {
        Integer vipLevel = Integer.valueOf(params.get("vipLevel").toString());
        Integer days = params.get("days") != null ? Integer.valueOf(params.get("days").toString()) : 30;
        adminService.updateUserVip(userId, vipLevel, days);
        return Result.success("VIP设置成功");
    }

    @DeleteMapping("/user/{userId}")
    public Result<Void> deleteUser(@PathVariable Long userId) {
        adminService.deleteUser(userId);
        return Result.success("删除成功");
    }

    @GetMapping("/vip-packages")
    public Result<List<VipPackage>> getAllVipPackages() {
        List<VipPackage> list = adminService.getAllVipPackages();
        return Result.success(list);
    }

    @PostMapping("/vip-package")
    public Result<VipPackage> saveVipPackage(@RequestBody VipPackage vipPackage) {
        VipPackage saved = adminService.saveVipPackage(vipPackage);
        return Result.success("保存成功", saved);
    }

    @DeleteMapping("/vip-package/{id}")
    public Result<Void> deleteVipPackage(@PathVariable Long id) {
        adminService.deleteVipPackage(id);
        return Result.success("删除成功");
    }

    @GetMapping("/activities")
    public Result<List<OfflineActivity>> getAllActivities() {
        List<OfflineActivity> list = adminService.getAllActivities();
        return Result.success(list);
    }

    @PostMapping("/activity")
    public Result<OfflineActivity> saveActivity(@RequestBody OfflineActivity activity) {
        OfflineActivity saved = adminService.saveActivity(activity);
        return Result.success("保存成功", saved);
    }

    @DeleteMapping("/activity/{id}")
    public Result<Void> deleteActivity(@PathVariable Long id) {
        adminService.deleteActivity(id);
        return Result.success("删除成功");
    }

    @GetMapping("/configs")
    public Result<List<SysConfig>> getAllConfigs() {
        List<SysConfig> list = adminService.getAllConfigs();
        return Result.success(list);
    }

    @PostMapping("/config")
    public Result<SysConfig> saveConfig(@RequestBody SysConfig config) {
        SysConfig saved = adminService.saveConfig(config);
        return Result.success("保存成功", saved);
    }

    @GetMapping("/operation-logs")
    public Result<Map<String, Object>> getOperationLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Map<String, Object> result = adminService.getOperationLogs(page, size);
        return Result.success(result);
    }

    @GetMapping("/sensitive-words")
    public Result<List<SensitiveWord>> getAllSensitiveWords() {
        List<SensitiveWord> list = adminService.getAllSensitiveWords();
        return Result.success(list);
    }

    @PostMapping("/sensitive-word")
    public Result<SensitiveWord> saveSensitiveWord(@RequestBody SensitiveWord word) {
        SensitiveWord saved = adminService.saveSensitiveWord(word);
        return Result.success("保存成功", saved);
    }

    @DeleteMapping("/sensitive-word/{id}")
    public Result<Void> deleteSensitiveWord(@PathVariable Long id) {
        adminService.deleteSensitiveWord(id);
        return Result.success("删除成功");
    }

    @GetMapping("/daily-tasks")
    public Result<List<DailyTask>> getAllDailyTasks() {
        List<DailyTask> list = adminService.getAllDailyTasks();
        return Result.success(list);
    }

    @PostMapping("/daily-task")
    public Result<DailyTask> saveDailyTask(@RequestBody DailyTask task) {
        DailyTask saved = adminService.saveDailyTask(task);
        return Result.success("保存成功", saved);
    }

    @DeleteMapping("/daily-task/{id}")
    public Result<Void> deleteDailyTask(@PathVariable Long id) {
        adminService.deleteDailyTask(id);
        return Result.success("删除成功");
    }

    @GetMapping("/virtual-gifts")
    public Result<List<VirtualGift>> getAllVirtualGifts() {
        List<VirtualGift> list = adminService.getAllVirtualGifts();
        return Result.success(list);
    }

    @PostMapping("/virtual-gift")
    public Result<VirtualGift> saveVirtualGift(@RequestBody VirtualGift gift) {
        VirtualGift saved = adminService.saveVirtualGift(gift);
        return Result.success("保存成功", saved);
    }

    @DeleteMapping("/virtual-gift/{id}")
    public Result<Void> deleteVirtualGift(@PathVariable Long id) {
        adminService.deleteVirtualGift(id);
        return Result.success("删除成功");
    }
}
