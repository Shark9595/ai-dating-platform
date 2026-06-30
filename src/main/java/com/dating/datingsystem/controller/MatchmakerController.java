package com.dating.datingsystem.controller;

import com.dating.datingsystem.common.Result;
import com.dating.datingsystem.entity.MatchmakerServiceEntity;
import com.dating.datingsystem.service.MatchmakerService;
import com.dating.datingsystem.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/matchmaker")
public class MatchmakerController {

    @Autowired
    private MatchmakerService matchmakerService;

    @Autowired
    private SecurityUtil securityUtil;

    @GetMapping("/services")
    public Result<List<Map<String, Object>>> getServiceList() {
        Long matchmakerId = securityUtil.getCurrentUserId();
        List<Map<String, Object>> list = matchmakerService.getServiceList(matchmakerId);
        return Result.success(list);
    }

    @GetMapping("/myServices")
    public Result<List<Map<String, Object>>> getMyServices() {
        Long userId = securityUtil.getCurrentUserId();
        List<Map<String, Object>> list = matchmakerService.getUserServices(userId);
        return Result.success(list);
    }

    @PostMapping("/order")
    public Result<MatchmakerServiceEntity> orderService(@RequestBody Map<String, Object> params) {
        Long userId = securityUtil.getCurrentUserId();
        Integer serviceType = Integer.valueOf(params.get("serviceType").toString());
        
        String[] serviceNames = {"", "人工牵线", "情感咨询", "约会指导"};
        String serviceName = serviceType < serviceNames.length ? serviceNames[serviceType] : "红娘服务";
        
        MatchmakerServiceEntity service = matchmakerService.createService(userId, serviceType, serviceName, null);
        return Result.success("预约成功", service);
    }

    @PostMapping("/service/create")
    public Result<MatchmakerServiceEntity> createService(@RequestBody Map<String, Object> params) {
        Long userId = securityUtil.getCurrentUserId();
        Integer serviceType = Integer.valueOf(params.get("serviceType").toString());
        String serviceName = params.get("serviceName").toString();
        String requirement = params.get("requirement") != null ? params.get("requirement").toString() : null;
        MatchmakerServiceEntity service = matchmakerService.createService(userId, serviceType, serviceName, requirement);
        return Result.success("服务申请提交成功", service);
    }

    @PutMapping("/service/{serviceId}")
    public Result<Void> updateServiceStatus(
            @PathVariable Long serviceId,
            @RequestBody Map<String, Object> params) {
        Long matchmakerId = securityUtil.getCurrentUserId();
        Integer status = Integer.valueOf(params.get("status").toString());
        String result = params.get("result") != null ? params.get("result").toString() : null;
        matchmakerService.updateServiceStatus(serviceId, matchmakerId, status, result);
        return Result.success("状态更新成功");
    }

    @GetMapping("/reports/pending")
    public Result<List<Map<String, Object>>> getPendingReports() {
        List<Map<String, Object>> list = matchmakerService.getPendingReports();
        return Result.success(list);
    }

    @PostMapping("/report/handle/{reportId}")
    public Result<Void> handleReport(
            @PathVariable Long reportId,
            @RequestBody Map<String, Object> params) {
        Long handlerId = securityUtil.getCurrentUserId();
        Integer status = Integer.valueOf(params.get("status").toString());
        String handleResult = params.get("handleResult") != null ? params.get("handleResult").toString() : null;
        matchmakerService.handleReport(reportId, handlerId, status, handleResult);
        return Result.success("处理完成");
    }

    @GetMapping("/posts/pending")
    public Result<List<Map<String, Object>>> getPendingPosts() {
        List<Map<String, Object>> list = matchmakerService.getPendingPosts();
        return Result.success(list);
    }

    @PostMapping("/post/audit/{postId}")
    public Result<Void> auditPost(
            @PathVariable Long postId,
            @RequestBody Map<String, Object> params) {
        Integer auditStatus = Integer.valueOf(params.get("auditStatus").toString());
        String auditRemark = params.get("auditRemark") != null ? params.get("auditRemark").toString() : null;
        matchmakerService.auditPost(postId, auditStatus, auditRemark);
        return Result.success("审核完成");
    }

    @GetMapping("/profiles/pending")
    public Result<List<Map<String, Object>>> getPendingProfiles() {
        List<Map<String, Object>> list = matchmakerService.getPendingProfiles();
        return Result.success(list);
    }

    @PostMapping("/profile/audit/{userId}")
    public Result<Void> auditProfile(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> params) {
        Integer auditStatus = Integer.valueOf(params.get("auditStatus").toString());
        matchmakerService.auditProfile(userId, auditStatus);
        return Result.success("审核完成");
    }
}
