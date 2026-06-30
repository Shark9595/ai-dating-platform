package com.dating.datingsystem.entity;

import lombok.Data;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "match_preference")
public class MatchPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Integer minAge;

    private Integer maxAge;

    private Integer minHeight;

    private Integer maxHeight;

    @Column(length = 50)
    private String education;

    @Column(length = 50)
    private String city;

    private BigDecimal minSalary;

    private Integer marriageStatus;

    @Column(length = 500)
    private String hobbies;

    @Column(length = 500)
    private String otherRequirements;

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
