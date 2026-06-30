package com.dating.datingsystem.service;

import com.dating.datingsystem.entity.ActivityRegistration;
import com.dating.datingsystem.entity.OfflineActivity;
import com.dating.datingsystem.entity.User;
import com.dating.datingsystem.repository.ActivityRegistrationRepository;
import com.dating.datingsystem.repository.OfflineActivityRepository;
import com.dating.datingsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ActivityService {

    @Autowired
    private OfflineActivityRepository activityRepository;

    @Autowired
    private ActivityRegistrationRepository registrationRepository;

    @Autowired
    private UserRepository userRepository;

    public List<OfflineActivity> getActivityList(int page, int size) {
        List<OfflineActivity> activities = activityRepository.findByStatusOrderByCreateTimeDesc(1);
        int start = page * size;
        int end = Math.min(start + size, activities.size());
        if (start >= activities.size()) {
            return new ArrayList<>();
        }
        return activities.subList(start, end);
    }

    public OfflineActivity getActivityDetail(Long activityId) {
        return activityRepository.findById(activityId)
                .orElseThrow(() -> new RuntimeException("活动不存在"));
    }

    @Transactional
    public ActivityRegistration registerActivity(Long userId, Long activityId) {
        OfflineActivity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new RuntimeException("活动不存在"));

        if (activity.getCurrentParticipants() >= activity.getMaxParticipants()) {
            throw new RuntimeException("活动名额已满");
        }

        if (registrationRepository.existsByActivityIdAndUserId(activityId, userId)) {
            throw new RuntimeException("已报名该活动");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        ActivityRegistration registration = new ActivityRegistration();
        registration.setActivityId(activityId);
        registration.setUserId(userId);

        BigDecimal price = activity.getPrice();
        if (user.getVipLevel() != null && user.getVipLevel() > 0 && activity.getVipPrice() != null) {
            price = activity.getVipPrice();
        }
        registration.setPayAmount(price);

        registration = registrationRepository.save(registration);

        activity.setCurrentParticipants(activity.getCurrentParticipants() + 1);
        activityRepository.save(activity);

        return registration;
    }

    public List<Map<String, Object>> getMyActivities(Long userId) {
        List<ActivityRegistration> registrations = registrationRepository.findByUserIdOrderByCreateTimeDesc(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (ActivityRegistration reg : registrations) {
            Map<String, Object> item = new HashMap<>();
            item.put("registration", reg);
            OfflineActivity activity = activityRepository.findById(reg.getActivityId()).orElse(null);
            item.put("activity", activity);
            result.add(item);
        }
        return result;
    }

    @Transactional
    public void submitFeedback(Long userId, Long registrationId, String feedback, Integer rating) {
        ActivityRegistration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("报名记录不存在"));

        if (!registration.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作");
        }

        registration.setFeedback(feedback);
        registration.setRating(rating);
        registrationRepository.save(registration);
    }

    public boolean isRegistered(Long userId, Long activityId) {
        return registrationRepository.existsByActivityIdAndUserId(activityId, userId);
    }
}
