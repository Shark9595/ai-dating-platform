package com.dating.datingsystem.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "sys_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, length = 20)
    private String phone;

    @Column(unique = true, length = 100)
    private String email;

    @Column(length = 50)
    private String nickname;

    @Column(length = 255)
    private String avatar;

    private Integer gender;

    private Integer age;

    @Column(length = 20)
    private String role;

    private Integer vipLevel;

    private LocalDateTime vipExpireTime;

    private Integer points;

    private Integer realNameStatus;

    private Integer status;

    private LocalDateTime lastLoginTime;

    @Column(length = 50)
    private String lastLoginIp;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer deleted;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
        if (status == null) status = 1;
        if (deleted == null) deleted = 0;
        if (role == null) role = "USER";
        if (vipLevel == null) vipLevel = 0;
        if (points == null) points = 0;
        if (realNameStatus == null) realNameStatus = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
