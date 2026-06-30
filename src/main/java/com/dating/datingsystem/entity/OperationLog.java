package com.dating.datingsystem.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "operation_log")
public class OperationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Column(length = 50)
    private String username;

    @Column(length = 50)
    private String module;

    @Column(length = 200)
    private String operation;

    @Column(length = 500)
    private String method;

    @Column(columnDefinition = "TEXT")
    private String params;

    @Column(length = 50)
    private String ip;

    private Integer status;

    @Column(columnDefinition = "TEXT")
    private String errorMsg;

    private LocalDateTime operateTime;

    @PrePersist
    protected void onCreate() {
        operateTime = LocalDateTime.now();
    }
}
