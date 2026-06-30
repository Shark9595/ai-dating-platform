package com.dating.datingsystem.dto;

import java.time.LocalDateTime;

/**
 * 降级响应DTO
 * 用于统一处理服务降级时的响应格式
 */
public class FallbackResponse {

    private Boolean success;
    private String message;
    private String fallbackType;
    private LocalDateTime timestamp;

    public FallbackResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public FallbackResponse(Boolean success, String message, String fallbackType) {
        this.success = success;
        this.message = message;
        this.fallbackType = fallbackType;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * 创建聊天服务降级响应
     */
    public static FallbackResponse chatFallback(String message) {
        return new FallbackResponse(false, message, "CHAT_SERVICE_FALLBACK");
    }

    /**
     * 创建匹配服务降级响应
     */
    public static FallbackResponse matchFallback(String message) {
        return new FallbackResponse(false, message, "MATCH_SERVICE_FALLBACK");
    }

    /**
     * 创建VIP服务降级响应
     */
    public static FallbackResponse vipFallback(String message) {
        return new FallbackResponse(false, message, "VIP_SERVICE_FALLBACK");
    }

    /**
     * 创建红娘服务降级响应
     */
    public static FallbackResponse matchmakerFallback(String message) {
        return new FallbackResponse(false, message, "MATCHMAKER_SERVICE_FALLBACK");
    }

    // Getters and Setters
    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFallbackType() {
        return fallbackType;
    }

    public void setFallbackType(String fallbackType) {
        this.fallbackType = fallbackType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "FallbackResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", fallbackType='" + fallbackType + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}