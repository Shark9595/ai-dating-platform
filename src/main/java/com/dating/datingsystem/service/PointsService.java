package com.dating.datingsystem.service;

import com.dating.datingsystem.entity.*;
import com.dating.datingsystem.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PointsService {

    @Autowired
    private PointsRecordRepository pointsRecordRepository;

    @Autowired
    private DailyTaskRepository dailyTaskRepository;

    @Autowired
    private UserTaskRepository userTaskRepository;

    @Autowired
    private UserRepository userRepository;

    public List<DailyTask> getDailyTasks() {
        return dailyTaskRepository.findByStatusOrderBySortAsc(1);
    }

    public List<Map<String, Object>> getUserDailyTasks(Long userId) {
        LocalDate today = LocalDate.now();
        List<DailyTask> tasks = dailyTaskRepository.findByStatusOrderBySortAsc(1);
        List<Map<String, Object>> result = new ArrayList<>();

        for (DailyTask task : tasks) {
            Map<String, Object> item = new HashMap<>();
            item.put("task", task);
            UserTask userTask = userTaskRepository.findByUserIdAndTaskIdAndTaskDate(userId, task.getId(), today).orElse(null);
            item.put("userTask", userTask);
            result.add(item);
        }
        return result;
    }

    @Transactional
    public void completeTask(Long userId, String taskKey) {
        LocalDate today = LocalDate.now();
        DailyTask task = dailyTaskRepository.findByStatusOrderBySortAsc(1).stream()
                .filter(t -> t.getTaskKey().equals(taskKey))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("任务不存在"));

        UserTask userTask = userTaskRepository.findByUserIdAndTaskIdAndTaskDate(userId, task.getId(), today).orElse(null);

        if (userTask != null && userTask.getStatus() == 1) {
            throw new RuntimeException("今日已完成该任务");
        }

        if (userTask == null) {
            userTask = new UserTask();
            userTask.setUserId(userId);
            userTask.setTaskId(task.getId());
            userTask.setTaskDate(today);
        }

        userTask.setProgress(100);
        userTask.setStatus(1);
        userTask.setCompleteTime(LocalDateTime.now());
        userTaskRepository.save(userTask);

        addPoints(userId, task.getPoints(), 1, "完成任务：" + task.getName());
    }

    @Transactional
    public void addPoints(Long userId, Integer points, Integer type, String description) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        user.setPoints(user.getPoints() + points);
        userRepository.save(user);

        PointsRecord record = new PointsRecord();
        record.setUserId(userId);
        record.setPoints(points);
        record.setType(type);
        record.setDescription(description);
        record.setExpireTime(LocalDateTime.now().plusMonths(12));
        pointsRecordRepository.save(record);
    }

    @Transactional
    public void deductPoints(Long userId, Integer points, String description) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (user.getPoints() < points) {
            throw new RuntimeException("积分不足");
        }

        user.setPoints(user.getPoints() - points);
        userRepository.save(user);

        PointsRecord record = new PointsRecord();
        record.setUserId(userId);
        record.setPoints(-points);
        record.setType(2);
        record.setDescription(description);
        pointsRecordRepository.save(record);
    }

    public List<PointsRecord> getPointsRecords(Long userId) {
        return pointsRecordRepository.findByUserIdOrderByCreateTimeDesc(userId);
    }

    public Integer getUserPoints(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        return user != null ? user.getPoints() : 0;
    }
}
