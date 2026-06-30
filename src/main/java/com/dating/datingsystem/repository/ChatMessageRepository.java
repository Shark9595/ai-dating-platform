package com.dating.datingsystem.repository;

import com.dating.datingsystem.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long>, JpaSpecificationExecutor<ChatMessage> {
    List<ChatMessage> findBySessionIdOrderBySendTimeDesc(Long sessionId);
    long countByReceiverIdAndIsRead(Long receiverId, Integer isRead);
}
