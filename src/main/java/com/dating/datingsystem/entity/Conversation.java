package com.dating.datingsystem.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 会话实体
 */
@Data
@Entity
@Table(name = "conversation")
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId1;

    private Long userId2;

    private Long lastMessageId;

    private LocalDateTime lastMessageTime;

    /**
     * userId1的未读消息数
     */
    private Integer unreadCount1;

    /**
     * userId2的未读消息数
     */
    private Integer unreadCount2;

    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        if (unreadCount1 == null) unreadCount1 = 0;
        if (unreadCount2 == null) unreadCount2 = 0;
    }
}