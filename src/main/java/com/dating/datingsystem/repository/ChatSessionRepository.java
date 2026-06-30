package com.dating.datingsystem.repository;

import com.dating.datingsystem.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long>, JpaSpecificationExecutor<ChatSession> {
    List<ChatSession> findByUser1IdOrUser2IdOrderByLastMessageTimeDesc(Long user1Id, Long user2Id);
    Optional<ChatSession> findByUser1IdAndUser2Id(Long user1Id, Long user2Id);
}
