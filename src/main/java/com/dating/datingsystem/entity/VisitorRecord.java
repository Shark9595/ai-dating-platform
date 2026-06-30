package com.dating.datingsystem.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "visitor_record")
public class VisitorRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long visitorId;

    private Long visitedId;

    private Integer isAnonymous;

    private LocalDateTime visitTime;

    @PrePersist
    protected void onCreate() {
        visitTime = LocalDateTime.now();
        if (isAnonymous == null) isAnonymous = 0;
    }
}
