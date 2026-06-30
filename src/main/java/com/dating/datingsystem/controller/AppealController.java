package com.dating.datingsystem.controller;

import com.dating.datingsystem.common.Result;
import com.dating.datingsystem.entity.Appeal;
import com.dating.datingsystem.repository.AppealRepository;
import com.dating.datingsystem.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 申诉控制器
 */
@RestController
@RequestMapping("/api/appeal")
public class AppealController {

    @Autowired
    private AppealRepository appealRepository;

    @Autowired
    private SecurityUtil securityUtil;

    /**
     * 创建申诉
     */
    @PostMapping("/create")
    public Result<Appeal> createAppeal(@RequestBody Map<String, Object> params) {
        Long userId = securityUtil.getCurrentUserId();

        Appeal appeal = new Appeal();
        appeal.setUserId(userId);
        appeal.setAppealType(Integer.valueOf(params.get("appealType").toString()));
        appeal.setTargetId(Long.valueOf(params.get("targetId").toString()));
        appeal.setTargetType(Integer.valueOf(params.get("targetType").toString()));
        appeal.setReason(params.get("reason").toString());
        appeal.setEvidence(params.get("evidence") != null ? params.get("evidence").toString() : null);
        appeal.setStatus(0);
        appeal.setCreateTime(LocalDateTime.now());

        Appeal saved = appealRepository.save(appeal);
        return Result.success("申诉已提交", saved);
    }

    /**
     * 获取我的申诉列表
     */
    @GetMapping("/my")
    public Result<List<Appeal>> getMyAppeals() {
        Long userId = securityUtil.getCurrentUserId();
        List<Appeal> appeals = appealRepository.findByUserIdOrderByCreateTimeDesc(userId);
        return Result.success(appeals);
    }

    /**
     * 获取申诉详情
     */
    @GetMapping("/{appealId}")
    public Result<Appeal> getAppealDetail(@PathVariable Long appealId) {
        Appeal appeal = appealRepository.findById(appealId)
                .orElseThrow(() -> new RuntimeException("申诉不存在"));
        return Result.success(appeal);
    }

    /**
     * 获取待处理申诉列表（管理员）
     */
    @GetMapping("/pending")
    public Result<List<Appeal>> getPendingAppeals() {
        List<Appeal> appeals = appealRepository.findByStatusOrderByCreateTimeDesc(0);
        return Result.success(appeals);
    }

    /**
     * 处理申诉（管理员）
     */
    @PostMapping("/handle/{appealId}")
    public Result<Void> handleAppeal(@PathVariable Long appealId, @RequestBody Map<String, Object> params) {
        Long userId = securityUtil.getCurrentUserId();

        Appeal appeal = appealRepository.findById(appealId)
                .orElseThrow(() -> new RuntimeException("申诉不存在"));

        appeal.setStatus(Integer.valueOf(params.get("status").toString()));
        appeal.setHandleResult(params.get("handleResult").toString());
        appeal.setHandlerId(userId);
        appeal.setHandleTime(LocalDateTime.now());
        appeal.setUpdateTime(LocalDateTime.now());

        appealRepository.save(appeal);
        return Result.success("处理成功");
    }
}
