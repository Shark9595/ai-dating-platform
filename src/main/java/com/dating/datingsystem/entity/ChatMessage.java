package com.dating.datingsystem.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "chat_message")
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long sessionId;

    private Long senderId;

    private Long receiverId;

    @Column(columnDefinition = "TEXT")
    private String content;

    private Integer messageType;

    private Integer isRead;

    private Integer isWithdraw;

    private LocalDateTime sendTime;

    @PrePersist
    protected void onCreate() {
        sendTime = LocalDateTime.now();
        if (isRead == null) isRead = 0;
        if (isWithdraw == null) isWithdraw = 0;
    }
}
