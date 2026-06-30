package com.dating.datingsystem.controller;

import com.dating.datingsystem.common.Result;
import com.dating.datingsystem.entity.ActivityRegistration;
import com.dating.datingsystem.entity.OfflineActivity;
import com.dating.datingsystem.service.ActivityService;
import com.dating.datingsystem.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/activity")
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    @Autowired
    private SecurityUtil securityUtil;

    @GetMapping("/list")
    public Result<List<OfflineActivity>> getActivityList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<OfflineActivity> list = activityService.getActivityList(page, size);
        return Result.success(list);
    }

    @GetMapping("/detail/{activityId}")
    public Result<OfflineActivity> getActivityDetail(@PathVariable Long activityId) {
        OfflineActivity activity = activityService.getActivityDetail(activityId);
        return Result.success(activity);
    }

    @PostMapping("/register/{activityId}")
    public Result<ActivityRegistration> registerActivity(@PathVariable Long activityId) {
        Long userId = securityUtil.getCurrentUserId();
        ActivityRegistration registration = activityService.registerActivity(userId, activityId);
        return Result.success("报名成功", registration);
    }

    @GetMapping("/my")
    public Result<List<Map<String, Object>>> getMyActivities() {
        Long userId = securityUtil.getCurrentUserId();
        List<Map<String, Object>> list = activityService.getMyActivities(userId);
        return Result.success(list);
    }

    @PostMapping("/feedback/{registrationId}")
    public Result<Void> submitFeedback(
            @PathVariable Long registrationId,
            @RequestBody Map<String, Object> params) {
        Long userId = securityUtil.getCurrentUserId();
        String feedback = params.get("feedback") != null ? params.get("feedback").toString() : null;
        Integer rating = params.get("rating") != null ? Integer.valueOf(params.get("rating").toString()) : null;
        activityService.submitFeedback(userId, registrationId, feedback, rating);
        return Result.success("反馈提交成功");
    }

    @GetMapping("/is-registered/{activityId}")
    public Result<Boolean> isRegistered(@PathVariable Long activityId) {
        Long userId = securityUtil.getCurrentUserId();
        boolean registered = activityService.isRegistered(userId, activityId);
        return Result.success(registered);
    }
}
