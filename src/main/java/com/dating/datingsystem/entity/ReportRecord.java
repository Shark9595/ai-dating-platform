package com.dating.datingsystem.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "report_record")
public class ReportRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long reporterId;

    private Long reportedId;

    private Integer reportType;

    @Column(length = 500)
    private String reason;

    @Column(length = 500)
    private String evidence;

    private Integer targetType;

    private Long targetId;

    private Integer status;

    private Long handlerId;

    @Column(length = 500)
    private String handleResult;

    private LocalDateTime handleTime;

    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        if (status == null) status = 0;
    }
}
