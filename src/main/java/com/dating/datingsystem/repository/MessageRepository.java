package com.dating.datingsystem.repository;

import com.dating.datingsystem.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long>, JpaSpecificationExecutor<Message> {

    /**
     * 根据会话ID查询消息列表，按时间升序
     */
    List<Message> findByConversationIdOrderByCreateTimeAsc(Long conversationId);

    /**
     * 根据会话ID查询消息列表，按时间降序
     */
    List<Message> findByConversationIdOrderByCreateTimeDesc(Long conversationId);

    /**
     * 查询用户收到的未读消息数量
     */
    long countByReceiverIdAndStatus(Long receiverId, Message.MessageStatus status);

    /**
     * 查询用户在指定会话中的未读消息数量
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversationId = :conversationId AND m.receiverId = :receiverId AND m.status = :status")
    long countByConversationIdAndReceiverIdAndStatus(@Param("conversationId") Long conversationId, @Param("receiverId") Long receiverId, @Param("status") Message.MessageStatus status);

    /**
     * 将会话中用户的所有未读消息标记为已读
     */
    @Modifying
    @Query("UPDATE Message m SET m.status = :status WHERE m.conversationId = :conversationId AND m.receiverId = :receiverId AND m.status = :oldStatus")
    int markAllAsRead(@Param("conversationId") Long conversationId, @Param("receiverId") Long receiverId, @Param("status") Message.MessageStatus status, @Param("oldStatus") Message.MessageStatus oldStatus);

    /**
     * 查询两个用户之间的消息
     */
    @Query("SELECT m FROM Message m WHERE (m.senderId = :userId1 AND m.receiverId = :userId2) OR (m.senderId = :userId2 AND m.receiverId = :userId1) ORDER BY m.createTime ASC")
    List<Message> findMessagesBetweenUsers(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    /**
     * 分页查询会话消息
     */
    @Query(value = "SELECT * FROM message WHERE conversation_id = :conversationId ORDER BY create_time DESC LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<Message> findByConversationIdWithPagination(@Param("conversationId") Long conversationId, @Param("limit") int limit, @Param("offset") int offset);
}