package com.dating.datingsystem.controller;

import com.dating.datingsystem.common.Result;
import com.dating.datingsystem.entity.ReportRecord;
import com.dating.datingsystem.service.ReportService;
import com.dating.datingsystem.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private SecurityUtil securityUtil;

    @PostMapping("/submit")
    public Result<ReportRecord> submitReport(@RequestBody Map<String, Object> params) {
        Long userId = securityUtil.getCurrentUserId();
        Long reportedId = Long.valueOf(params.get("reportedId").toString());
        Integer reportType = Integer.valueOf(params.get("reportType").toString());
        Integer targetType = params.get("targetType") != null ? Integer.valueOf(params.get("targetType").toString()) : null;
        Long targetId = params.get("targetId") != null ? Long.valueOf(params.get("targetId").toString()) : null;
        String reason = params.get("reason") != null ? params.get("reason").toString() : null;
        String evidence = params.get("evidence") != null ? params.get("evidence").toString() : null;
        ReportRecord report = reportService.submitReport(userId, reportedId, reportType, targetType, targetId, reason, evidence);
        return Result.success("举报成功", report);
    }

    @GetMapping("/my")
    public Result<List<Map<String, Object>>> getMyReports() {
        Long userId = securityUtil.getCurrentUserId();
        List<Map<String, Object>> list = reportService.getMyReports(userId);
        return Result.success(list);
    }

    @PostMapping("/block/{userId}")
    public Result<Void> blockUser(@PathVariable Long userId) {
        Long currentUserId = securityUtil.getCurrentUserId();
        reportService.blockUser(currentUserId, userId);
        return Result.success("拉黑成功");
    }

    @PostMapping("/unblock/{userId}")
    public Result<Void> unblockUser(@PathVariable Long userId) {
        Long currentUserId = securityUtil.getCurrentUserId();
        reportService.unblockUser(currentUserId, userId);
        return Result.success("取消拉黑成功");
    }

    @GetMapping("/blacklist")
    public Result<List<Map<String, Object>>> getBlacklist() {
        Long userId = securityUtil.getCurrentUserId();
        List<Map<String, Object>> list = reportService.getBlacklist(userId);
        return Result.success(list);
    }

    @GetMapping("/is-blocked/{userId}")
    public Result<Boolean> isBlocked(@PathVariable Long userId) {
        Long currentUserId = securityUtil.getCurrentUserId();
        boolean blocked = reportService.isBlocked(currentUserId, userId);
        return Result.success(blocked);
    }
}
