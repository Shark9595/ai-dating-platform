package com.dating.datingsystem.controller;

import com.dating.datingsystem.common.Result;
import com.dating.datingsystem.entity.DailyTask;
import com.dating.datingsystem.entity.PointsRecord;
import com.dating.datingsystem.service.PointsService;
import com.dating.datingsystem.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/points")
public class PointsController {

    @Autowired
    private PointsService pointsService;

    @Autowired
    private SecurityUtil securityUtil;

    @GetMapping("/tasks")
    public Result<List<Map<String, Object>>> getDailyTasks() {
        Long userId = securityUtil.getCurrentUserId();
        List<Map<String, Object>> list = pointsService.getUserDailyTasks(userId);
        return Result.success(list);
    }

    @PostMapping("/task/complete/{taskKey}")
    public Result<Void> completeTask(@PathVariable String taskKey) {
        Long userId = securityUtil.getCurrentUserId();
        pointsService.completeTask(userId, taskKey);
        return Result.success("任务完成，积分已发放");
    }

    @GetMapping("/records")
    public Result<List<PointsRecord>> getPointsRecords() {
        Long userId = securityUtil.getCurrentUserId();
        List<PointsRecord> list = pointsService.getPointsRecords(userId);
        return Result.success(list);
    }

    @GetMapping("/balance")
    public Result<Integer> getUserPoints() {
        Long userId = securityUtil.getCurrentUserId();
        Integer points = pointsService.getUserPoints(userId);
        return Result.success(points);
    }
}
