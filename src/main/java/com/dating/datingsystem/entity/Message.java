package com.dating.datingsystem.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 实时消息实体
 */
@Data
@Entity
@Table(name = "message")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long senderId;

    private Long receiverId;

    @Column(columnDefinition = "TEXT")
    private String content;

    /**
     * 消息类型: TEXT-文本, IMAGE-图片, GIFT-礼物
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private MessageType type;

    /**
     * 消息状态: UNREAD-未读, READ-已读
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private MessageStatus status;

    private Long conversationId;

    private LocalDateTime createTime;

    public enum MessageType {
        TEXT,
        IMAGE,
        GIFT
    }

    public enum MessageStatus {
        UNREAD,
        READ
    }

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        if (type == null) type = MessageType.TEXT;
        if (status == null) status = MessageStatus.UNREAD;
    }
}