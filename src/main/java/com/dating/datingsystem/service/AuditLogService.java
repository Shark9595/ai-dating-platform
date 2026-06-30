package com.dating.datingsystem.service;

import com.alibaba.fastjson.JSON;
import com.dating.datingsystem.entity.AuditLog;
import com.dating.datingsystem.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 审计日志服务
 */
@Service
public class AuditLogService {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogService.class);

    @Autowired
    private AuditLogRepository auditLogRepository;

    /**
     * 操作类型常量
     */
    public static final String ACTION_SEND_MESSAGE = "SEND_MESSAGE";
    public static final String ACTION_LOGIN = "LOGIN";
    public static final String ACTION_LOGOUT = "LOGOUT";
    public static final String ACTION_MODIFY_PROFILE = "MODIFY_PROFILE";
    public static final String ACTION_VIP_PURCHASE = "VIP_PURCHASE";
    public static final String ACTION_GIFT_SEND = "GIFT_SEND";
    public static final String ACTION_POST_CREATE = "POST_CREATE";
    public static final String ACTION_POST_DELETE = "POST_DELETE";
    public static final String ACTION_COMMENT_CREATE = "COMMENT_CREATE";
    public static final String ACTION_SENSITIVE_WORD_BLOCKED = "SENSITIVE_WORD_BLOCKED";
    public static final String ACTION_MATCHMAKER_ORDER = "MATCHMAKER_ORDER";
    public static final String ACTION_CHANGE_PASSWORD = "CHANGE_PASSWORD";
    public static final String ACTION_UPDATE_AVATAR = "UPDATE_AVATAR";
    public static final String ACTION_UPDATE_NICKNAME = "UPDATE_NICKNAME";
    public static final String ACTION_REAL_NAME_AUTH = "REAL_NAME_AUTH";

    /**
     * 资源类型常量
     */
    public static final String RESOURCE_MESSAGE = "MESSAGE";
    public static final String RESOURCE_USER = "USER";
    public static final String RESOURCE_VIP = "VIP";
    public static final String RESOURCE_GIFT = "GIFT";
    public static final String RESOURCE_POST = "POST";
    public static final String RESOURCE_COMMENT = "COMMENT";
    public static final String RESOURCE_MATCHMAKER_SERVICE = "MATCHMAKER_SERVICE";

    /**
     * 状态常量
     */
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_BLOCKED = "BLOCKED";

    /**
     * 记录消息发送
     */
    @Async
    @Transactional
    public void logMessageSent(Long userId, Long messageId, String content, String status) {
        try {
            String detail = buildMessageDetail(content);
            AuditLog auditLog = new AuditLog(
                    userId,
                    ACTION_SEND_MESSAGE,
                    RESOURCE_MESSAGE,
                    messageId,
                    detail,
                    null,
                    null,
                    status
            );
            auditLogRepository.save(auditLog);
            logger.info("记录消息发送日志: userId={}, messageId={}, status={}", userId, messageId, status);
        } catch (Exception e) {
            logger.error("记录消息发送日志失败: userId={}, messageId={}", userId, messageId, e);
        }
    }

    /**
     * 记录消息发送（带IP和User-Agent）
     */
    @Async
    @Transactional
    public void logMessageSent(Long userId, Long messageId, String content, String status,
                                String ipAddress, String userAgent) {
        try {
            String detail = buildMessageDetail(content);
            AuditLog auditLog = new AuditLog(
                    userId,
                    ACTION_SEND_MESSAGE,
                    RESOURCE_MESSAGE,
                    messageId,
                    detail,
                    ipAddress,
                    userAgent,
                    status
            );
            auditLogRepository.save(auditLog);
            logger.info("记录消息发送日志: userId={}, messageId={}, status={}", userId, messageId, status);
        } catch (Exception e) {
            logger.error("记录消息发送日志失败: userId={}, messageId={}", userId, messageId, e);
        }
    }

    /**
     * 记录敏感词拦截
     */
    @Async
    @Transactional
    public void logSensitiveWordBlocked(Long userId, String content, List<String> blockedWords) {
        try {
            Map<String, Object> detailMap = new HashMap<>();
            detailMap.put("content", truncateContent(content, 200));
            detailMap.put("blockedWords", blockedWords);
            String detail = JSON.toJSONString(detailMap);

            AuditLog auditLog = new AuditLog(
                    userId,
                    ACTION_SENSITIVE_WORD_BLOCKED,
                    RESOURCE_MESSAGE,
                    null,
                    detail,
                    null,
                    null,
                    STATUS_BLOCKED
            );
            auditLogRepository.save(auditLog);
            logger.warn("记录敏感词拦截日志: userId={}, blockedWords={}", userId, blockedWords);
        } catch (Exception e) {
            logger.error("记录敏感词拦截日志失败: userId={}", userId, e);
        }
    }

    /**
     * 记录敏感词拦截（带IP和User-Agent）
     */
    @Async
    @Transactional
    public void logSensitiveWordBlocked(Long userId, String content, List<String> blockedWords,
                                         String ipAddress, String userAgent) {
        try {
            Map<String, Object> detailMap = new HashMap<>();
            detailMap.put("content", truncateContent(content, 200));
            detailMap.put("blockedWords", blockedWords);
            String detail = JSON.toJSONString(detailMap);

            AuditLog auditLog = new AuditLog(
                    userId,
                    ACTION_SENSITIVE_WORD_BLOCKED,
                    RESOURCE_MESSAGE,
                    null,
                    detail,
                    ipAddress,
                    userAgent,
                    STATUS_BLOCKED
            );
            auditLogRepository.save(auditLog);
            logger.warn("记录敏感词拦截日志: userId={}, blockedWords={}", userId, blockedWords);
        } catch (Exception e) {
            logger.error("记录敏感词拦截日志失败: userId={}", userId, e);
        }
    }

    /**
     * 记录资料修改
     */
    @Async
    @Transactional
    public void logProfileModified(Long userId, Map<String, Object> modifiedFields) {
        try {
            String detail = JSON.toJSONString(modifiedFields);
            AuditLog auditLog = new AuditLog(
                    userId,
                    ACTION_MODIFY_PROFILE,
                    RESOURCE_USER,
                    userId,
                    detail,
                    null,
                    null,
                    STATUS_SUCCESS
            );
            auditLogRepository.save(auditLog);
            logger.info("记录资料修改日志: userId={}, fields={}", userId, modifiedFields.keySet());
        } catch (Exception e) {
            logger.error("记录资料修改日志失败: userId={}", userId, e);
        }
    }

    /**
     * 记录资料修改（带IP和User-Agent）
     */
    @Async
    @Transactional
    public void logProfileModified(Long userId, Map<String, Object> modifiedFields,
                                    String ipAddress, String userAgent) {
        try {
            String detail = JSON.toJSONString(modifiedFields);
            AuditLog auditLog = new AuditLog(
                    userId,
                    ACTION_MODIFY_PROFILE,
                    RESOURCE_USER,
                    userId,
                    detail,
                    ipAddress,
                    userAgent,
                    STATUS_SUCCESS
            );
            auditLogRepository.save(auditLog);
            logger.info("记录资料修改日志: userId={}, fields={}", userId, modifiedFields.keySet());
        } catch (Exception e) {
            logger.error("记录资料修改日志失败: userId={}", userId, e);
        }
    }

    /**
     * 记录VIP购买
     */
    @Async
    @Transactional
    public void logVipPurchase(Long userId, Long packageId, String status) {
        try {
            Map<String, Object> detailMap = new HashMap<>();
            detailMap.put("packageId", packageId);
            String detail = JSON.toJSONString(detailMap);

            AuditLog auditLog = new AuditLog(
                    userId,
                    ACTION_VIP_PURCHASE,
                    RESOURCE_VIP,
                    packageId,
                    detail,
                    null,
                    null,
                    status
            );
            auditLogRepository.save(auditLog);
            logger.info("记录VIP购买日志: userId={}, packageId={}, status={}", userId, packageId, status);
        } catch (Exception e) {
            logger.error("记录VIP购买日志失败: userId={}, packageId={}", userId, packageId, e);
        }
    }

    /**
     * 记录VIP购买（带IP和User-Agent）
     */
    @Async
    @Transactional
    public void logVipPurchase(Long userId, Long packageId, String status,
                                String ipAddress, String userAgent) {
        try {
            Map<String, Object> detailMap = new HashMap<>();
            detailMap.put("packageId", packageId);
            String detail = JSON.toJSONString(detailMap);

            AuditLog auditLog = new AuditLog(
                    userId,
                    ACTION_VIP_PURCHASE,
                    RESOURCE_VIP,
                    packageId,
                    detail,
                    ipAddress,
                    userAgent,
                    status
            );
            auditLogRepository.save(auditLog);
            logger.info("记录VIP购买日志: userId={}, packageId={}, status={}", userId, packageId, status);
        } catch (Exception e) {
            logger.error("记录VIP购买日志失败: userId={}, packageId={}", userId, packageId, e);
        }
    }

    /**
     * 记录登录
     */
    @Async
    @Transactional
    public void logLogin(Long userId, String ipAddress, String userAgent, String status) {
        try {
            AuditLog auditLog = new AuditLog(
                    userId,
                    ACTION_LOGIN,
                    RESOURCE_USER,
                    userId,
                    "用户登录",
                    ipAddress,
                    userAgent,
                    status
            );
            auditLogRepository.save(auditLog);
            logger.info("记录登录日志: userId={}, status={}, ip={}", userId, status, ipAddress);
        } catch (Exception e) {
            logger.error("记录登录日志失败: userId={}", userId, e);
        }
    }

    /**
     * 记录登出
     */
    @Async
    @Transactional
    public void logLogout(Long userId, String ipAddress) {
        try {
            AuditLog auditLog = new AuditLog(
                    userId,
                    ACTION_LOGOUT,
                    RESOURCE_USER,
                    userId,
                    "用户登出",
                    ipAddress,
                    null,
                    STATUS_SUCCESS
            );
            auditLogRepository.save(auditLog);
            logger.info("记录登出日志: userId={}, ip={}", userId, ipAddress);
        } catch (Exception e) {
            logger.error("记录登出日志失败: userId={}", userId, e);
        }
    }

    /**
     * 记录礼物发送
     */
    @Async
    @Transactional
    public void logGiftSent(Long userId, Long giftId, Long receiverId, Integer quantity, String status) {
        try {
            Map<String, Object> detailMap = new HashMap<>();
            detailMap.put("giftId", giftId);
            detailMap.put("receiverId", receiverId);
            detailMap.put("quantity", quantity);
            String detail = JSON.toJSONString(detailMap);

            AuditLog auditLog = new AuditLog(
                    userId,
                    ACTION_GIFT_SEND,
                    RESOURCE_GIFT,
                    giftId,
                    detail,
                    null,
                    null,
                    status
            );
            auditLogRepository.save(auditLog);
            logger.info("记录礼物发送日志: userId={}, giftId={}, receiverId={}", userId, giftId, receiverId);
        } catch (Exception e) {
            logger.error("记录礼物发送日志失败: userId={}, giftId={}", userId, giftId, e);
        }
    }

    /**
     * 记录帖子创建
     */
    @Async
    @Transactional
    public void logPostCreated(Long userId, Long postId, String content) {
        try {
            String detail = truncateContent(content, 200);
            AuditLog auditLog = new AuditLog(
                    userId,
                    ACTION_POST_CREATE,
                    RESOURCE_POST,
                    postId,
                    detail,
                    null,
                    null,
                    STATUS_SUCCESS
            );
            auditLogRepository.save(auditLog);
            logger.info("记录帖子创建日志: userId={}, postId={}", userId, postId);
        } catch (Exception e) {
            logger.error("记录帖子创建日志失败: userId={}, postId={}", userId, postId, e);
        }
    }

    /**
     * 记录帖子删除
     */
    @Async
    @Transactional
    public void logPostDeleted(Long userId, Long postId) {
        try {
            AuditLog auditLog = new AuditLog(
                    userId,
                    ACTION_POST_DELETE,
                    RESOURCE_POST,
                    postId,
                    "删除帖子",
                    null,
                    null,
                    STATUS_SUCCESS
            );
            auditLogRepository.save(auditLog);
            logger.info("记录帖子删除日志: userId={}, postId={}", userId, postId);
        } catch (Exception e) {
            logger.error("记录帖子删除日志失败: userId={}, postId={}", userId, postId, e);
        }
    }

    /**
     * 记录密码修改
     */
    @Async
    @Transactional
    public void logPasswordChanged(Long userId, String ipAddress) {
        try {
            AuditLog auditLog = new AuditLog(
                    userId,
                    ACTION_CHANGE_PASSWORD,
                    RESOURCE_USER,
                    userId,
                    "修改密码",
                    ipAddress,
                    null,
                    STATUS_SUCCESS
            );
            auditLogRepository.save(auditLog);
            logger.info("记录密码修改日志: userId={}, ip={}", userId, ipAddress);
        } catch (Exception e) {
            logger.error("记录密码修改日志失败: userId={}", userId, e);
        }
    }

    /**
     * 记录头像更新
     */
    @Async
    @Transactional
    public void logAvatarUpdated(Long userId, String avatar) {
        try {
            Map<String, Object> detailMap = new HashMap<>();
            detailMap.put("avatar", avatar);
            String detail = JSON.toJSONString(detailMap);

            AuditLog auditLog = new AuditLog(
                    userId,
                    ACTION_UPDATE_AVATAR,
                    RESOURCE_USER,
                    userId,
                    detail,
                    null,
                    null,
                    STATUS_SUCCESS
            );
            auditLogRepository.save(auditLog);
            logger.info("记录头像更新日志: userId={}", userId);
        } catch (Exception e) {
            logger.error("记录头像更新日志失败: userId={}", userId, e);
        }
    }

    /**
     * 记录昵称更新
     */
    @Async
    @Transactional
    public void logNicknameUpdated(Long userId, String nickname) {
        try {
            Map<String, Object> detailMap = new HashMap<>();
            detailMap.put("nickname", nickname);
            String detail = JSON.toJSONString(detailMap);

            AuditLog auditLog = new AuditLog(
                    userId,
                    ACTION_UPDATE_NICKNAME,
                    RESOURCE_USER,
                    userId,
                    detail,
                    null,
                    null,
                    STATUS_SUCCESS
            );
            auditLogRepository.save(auditLog);
            logger.info("记录昵称更新日志: userId={}, nickname={}", userId, nickname);
        } catch (Exception e) {
            logger.error("记录昵称更新日志失败: userId={}", userId, e);
        }
    }

    /**
     * 记录实名认证
     */
    @Async
    @Transactional
    public void logRealNameAuth(Long userId, String status) {
        try {
            AuditLog auditLog = new AuditLog(
                    userId,
                    ACTION_REAL_NAME_AUTH,
                    RESOURCE_USER,
                    userId,
                    "实名认证提交",
                    null,
                    null,
                    status
            );
            auditLogRepository.save(auditLog);
            logger.info("记录实名认证日志: userId={}, status={}", userId, status);
        } catch (Exception e) {
            logger.error("记录实名认证日志失败: userId={}", userId, e);
        }
    }

    /**
     * 记录红娘服务订单
     */
    @Async
    @Transactional
    public void logMatchmakerOrder(Long userId, Long serviceId, String status) {
        try {
            AuditLog auditLog = new AuditLog(
                    userId,
                    ACTION_MATCHMAKER_ORDER,
                    RESOURCE_MATCHMAKER_SERVICE,
                    serviceId,
                    "预约红娘服务",
                    null,
                    null,
                    status
            );
            auditLogRepository.save(auditLog);
            logger.info("记录红娘服务订单日志: userId={}, serviceId={}, status={}", userId, serviceId, status);
        } catch (Exception e) {
            logger.error("记录红娘服务订单日志失败: userId={}, serviceId={}", userId, serviceId, e);
        }
    }

    /**
     * 查询用户审计日志
     */
    public Page<AuditLog> getUserAuditLogs(Long userId, Pageable pageable) {
        return auditLogRepository.findByUserId(userId, pageable);
    }

    /**
     * 查询用户指定操作的审计日志
     */
    public Page<AuditLog> getUserActionLogs(Long userId, String action, Pageable pageable) {
        return auditLogRepository.findByUserIdAndAction(userId, action, pageable);
    }

    /**
     * 获取用户最近的操作记录
     */
    public List<AuditLog> getRecentUserActions(Long userId) {
        return auditLogRepository.findTop10ByUserIdOrderByCreateTimeDesc(userId);
    }

    /**
     * 统计用户指定时间段内的操作次数
     */
    public Long countUserActionSince(Long userId, String action, LocalDateTime startTime) {
        return auditLogRepository.countUserActionSince(userId, action, startTime);
    }

    /**
     * 构建消息详情
     */
    private String buildMessageDetail(String content) {
        Map<String, Object> detailMap = new HashMap<>();
        detailMap.put("content", truncateContent(content, 100));
        return JSON.toJSONString(detailMap);
    }

    /**
     * 截断内容，防止日志过长
     */
    private String truncateContent(String content, int maxLength) {
        if (content == null) {
            return null;
        }
        return content.length() > maxLength ? content.substring(0, maxLength) + "..." : content;
    }
}