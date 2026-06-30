package com.dating.datingsystem.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 幂等性令牌实体
 * 用于防止重复提交
 */
@Entity
@Table(name = "idempotent_token")
public class IdempotentToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token", nullable = false, unique = true, length = 64)
    private String token;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "uri", nullable = false, length = 255)
    private String uri;

    @Column(name = "method", nullable = false, length = 10)
    private String method;

    @Column(name = "status", nullable = false)
    private Integer status;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @Column(name = "expire_time", nullable = false)
    private LocalDateTime expireTime;

    @Column(name = "result", columnDefinition = "TEXT")
    private String result;

    public IdempotentToken() {}

    public IdempotentToken(String token, Long userId, String uri, String method, Integer status, 
                           LocalDateTime createTime, LocalDateTime expireTime) {
        this.token = token;
        this.userId = userId;
        this.uri = uri;
        this.method = method;
        this.status = status;
        this.createTime = createTime;
        this.expireTime = expireTime;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUri() { return uri; }
    public void setUri(String uri) { this.uri = uri; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getExpireTime() { return expireTime; }
    public void setExpireTime(LocalDateTime expireTime) { this.expireTime = expireTime; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
}
