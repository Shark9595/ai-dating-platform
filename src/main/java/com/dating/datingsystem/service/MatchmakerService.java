package com.dating.datingsystem.service;

import com.dating.datingsystem.entity.*;
import com.dating.datingsystem.repository.*;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MatchmakerService {

    private static final Logger logger = LoggerFactory.getLogger(MatchmakerService.class);

    @Autowired
    private MatchmakerServiceRepository matchmakerServiceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReportRecordRepository reportRecordRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    public List<Map<String, Object>> getServiceList(Long matchmakerId) {
        List<MatchmakerServiceEntity> services =
                matchmakerServiceRepository.findByMatchmakerIdOrderByCreateTimeDesc(matchmakerId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (MatchmakerServiceEntity service : services) {
            Map<String, Object> item = new HashMap<>();
            item.put("service", service);
            User user = userRepository.findById(service.getUserId()).orElse(null);
            item.put("user", user);
            result.add(item);
        }
        return result;
    }

    public List<Map<String, Object>> getUserServices(Long userId) {
        List<MatchmakerServiceEntity> services =
                matchmakerServiceRepository.findByUserIdOrderByCreateTimeDesc(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (MatchmakerServiceEntity service : services) {
            Map<String, Object> item = new HashMap<>();
            item.put("service", service);
            User matchmaker = userRepository.findById(service.getMatchmakerId()).orElse(null);
            item.put("matchmaker", matchmaker);
            result.add(item);
        }
        return result;
    }

    @Transactional
    @CircuitBreaker(name = "matchmaker", fallbackMethod = "createServiceFallback")
    public MatchmakerServiceEntity createService(Long userId, Integer serviceType,
                                                  String serviceName, String requirement) {
        logger.info("创建红娘服务: userId={}, serviceType={}", userId, serviceType);
        MatchmakerServiceEntity service = new MatchmakerServiceEntity();
        service.setUserId(userId);
        service.setServiceType(serviceType);
        service.setServiceName(serviceName);
        service.setRequirement(requirement);
        return matchmakerServiceRepository.save(service);
    }

    @Transactional
    @CircuitBreaker(name = "matchmaker", fallbackMethod = "updateServiceStatusFallback")
    public void updateServiceStatus(Long serviceId, Long matchmakerId, Integer status, String result) {
        logger.info("更新服务状态: serviceId={}, matchmakerId={}, status={}", serviceId, matchmakerId, status);
        MatchmakerServiceEntity service = matchmakerServiceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("服务记录不存在"));

        service.setMatchmakerId(matchmakerId);
        service.setStatus(status);
        service.setResult(result);
        if (status == 1) {
            service.setStartTime(LocalDateTime.now());
        } else if (status == 2) {
            service.setEndTime(LocalDateTime.now());
        }
        matchmakerServiceRepository.save(service);
    }

    /**
     * 创建服务降级方法
     */
    private MatchmakerServiceEntity createServiceFallback(Long userId, Integer serviceType,
                                                           String serviceName, String requirement, Exception e) {
        logger.error("创建红娘服务降级: userId={}, serviceType={}, error={}", userId, serviceType, e.getMessage());
        throw new RuntimeException("红娘预约服务暂时不可用，请稍后重试");
    }

    /**
     * 更新服务状态降级方法
     */
    private void updateServiceStatusFallback(Long serviceId, Long matchmakerId, Integer status, String result, Exception e) {
        logger.error("更新服务状态降级: serviceId={}, matchmakerId={}, error={}", serviceId, matchmakerId, e.getMessage());
        throw new RuntimeException("服务状态更新失败，请稍后重试");
    }

    public List<Map<String, Object>> getPendingReports() {
        List<ReportRecord> reports = reportRecordRepository.findByStatusOrderByCreateTimeDesc(0);
        List<Map<String, Object>> result = new ArrayList<>();
        for (ReportRecord report : reports) {
            Map<String, Object> item = new HashMap<>();
            item.put("report", report);
            User reporter = userRepository.findById(report.getReporterId()).orElse(null);
            User reported = userRepository.findById(report.getReportedId()).orElse(null);
            item.put("reporter", reporter);
            item.put("reported", reported);
            result.add(item);
        }
        return result;
    }

    @Transactional
    public void handleReport(Long reportId, Long handlerId, Integer status, String handleResult) {
        ReportRecord report = reportRecordRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("举报记录不存在"));

        report.setStatus(status);
        report.setHandlerId(handlerId);
        report.setHandleResult(handleResult);
        report.setHandleTime(LocalDateTime.now());
        reportRecordRepository.save(report);
    }

    public List<Map<String, Object>> getPendingPosts() {
        List<Post> posts = postRepository.findByStatusAndAuditStatusOrderByCreateTimeDesc(1, 0);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Post post : posts) {
            Map<String, Object> item = new HashMap<>();
            item.put("post", post);
            User user = userRepository.findById(post.getUserId()).orElse(null);
            item.put("user", user);
            result.add(item);
        }
        return result;
    }

    @Transactional
    public void auditPost(Long postId, Integer auditStatus, String auditRemark) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("动态不存在"));

        post.setAuditStatus(auditStatus);
        post.setAuditRemark(auditRemark);
        postRepository.save(post);
    }

    public List<Map<String, Object>> getPendingProfiles() {
        List<User> users = userRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (User user : users) {
            if (user.getRealNameStatus() != null && user.getRealNameStatus() == 1) {
                Map<String, Object> item = new HashMap<>();
                item.put("user", user);
                UserProfile profile = userProfileRepository.findByUserId(user.getId()).orElse(null);
                item.put("profile", profile);
                result.add(item);
            }
        }
        return result;
    }

    @Transactional
    public void auditProfile(Long userId, Integer auditStatus) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setRealNameStatus(auditStatus);
        userRepository.save(user);
    }
}
