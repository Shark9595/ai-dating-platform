package com.dating.datingsystem.repository;

import com.dating.datingsystem.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long>, JpaSpecificationExecutor<Conversation> {

    /**
     * 查询用户参与的所有会话，按最后消息时间降序
     */
    @Query("SELECT c FROM Conversation c WHERE c.userId1 = :userId OR c.userId2 = :userId ORDER BY c.lastMessageTime DESC")
    List<Conversation> findByUserIdOrderByLastMessageTimeDesc(@Param("userId") Long userId);

    /**
     * 查询两个用户之间的会话
     */
    @Query("SELECT c FROM Conversation c WHERE (c.userId1 = :userId1 AND c.userId2 = :userId2) OR (c.userId1 = :userId2 AND c.userId2 = :userId1)")
    Optional<Conversation> findByUsers(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    /**
     * 查询用户与另一用户之间的会话
     */
    Optional<Conversation> findByUserId1AndUserId2(Long userId1, Long userId2);
}