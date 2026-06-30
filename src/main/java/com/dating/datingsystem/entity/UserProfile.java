package com.dating.datingsystem.entity;

import lombok.Data;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_profile")
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Column(length = 20)
    private String realName;

    @Column(length = 18)
    private String idCard;

    private Integer height;

    private Integer weight;

    @Column(length = 50)
    private String education;

    @Column(length = 50)
    private String occupation;

    @Column(length = 50)
    private String company;

    private BigDecimal salary;

    @Column(length = 50)
    private String city;

    @Column(length = 50)
    private String district;

    @Column(length = 255)
    private String hometown;

    private Integer marriageStatus;

    private Integer hasChild;

    @Column(length = 255)
    private String hobbies;

    @Column(length = 500)
    private String introduction;

    @Column(length = 50)
    private String constellation;

    @Column(length = 50)
    private String bloodType;

    private Integer smoke;

    private Integer drink;

    @Column(length = 255)
    private String photos;

    @Column(length = 255)
    private String videoIntro;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
