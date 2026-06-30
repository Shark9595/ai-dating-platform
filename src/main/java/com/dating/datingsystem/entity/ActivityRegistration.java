package com.dating.datingsystem.entity;

import lombok.Data;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "activity_registration")
public class ActivityRegistration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long activityId;

    private Long userId;

    private BigDecimal payAmount;

    private Integer payStatus;

    private Integer signInStatus;

    private Integer status;

    @Column(length = 500)
    private String feedback;

    private Integer rating;

    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        if (status == null) status = 1;
        if (payStatus == null) payStatus = 0;
        if (signInStatus == null) signInStatus = 0;
    }
}
