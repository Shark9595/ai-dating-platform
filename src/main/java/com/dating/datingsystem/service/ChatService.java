package com.dating.datingsystem.service;

import com.dating.datingsystem.entity.ChatMessage;
import com.dating.datingsystem.entity.ChatSession;
import com.dating.datingsystem.entity.User;
import com.dating.datingsystem.repository.ChatMessageRepository;
import com.dating.datingsystem.repository.ChatSessionRepository;
import com.dating.datingsystem.repository.UserRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    @Autowired
    private ChatSessionRepository chatSessionRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContentFilterService contentFilterService;

    public List<Map<String, Object>> getChatSessions(Long userId) {
        List<ChatSession> sessions = chatSessionRepository.findByUser1IdOrUser2IdOrderByLastMessageTimeDesc(userId, userId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (ChatSession session : sessions) {
            Map<String, Object> item = new HashMap<>();
            item.put("session", session);
            Long otherUserId = session.getUser1Id().equals(userId) ? session.getUser2Id() : session.getUser1Id();
            User otherUser = userRepository.findById(otherUserId).orElse(null);
            item.put("otherUser", otherUser);
            long unreadCount = chatMessageRepository.countByReceiverIdAndIsRead(userId, 0);
            item.put("unreadCount", unreadCount);
            result.add(item);
        }
        return result;
    }

    public List<ChatMessage> getChatMessages(Long sessionId, int page, int size) {
        List<ChatMessage> messages = chatMessageRepository.findBySessionIdOrderBySendTimeDesc(sessionId);
        int start = page * size;
        int end = Math.min(start + size, messages.size());
        if (start >= messages.size()) {
            return new ArrayList<>();
        }
        return messages.subList(start, end);
    }

    @Transactional
    @CircuitBreaker(name = "chat", fallbackMethod = "sendMessageFallback")
    @RateLimiter(name = "chat", fallbackMethod = "sendMessageRateLimitFallback")
    public ChatMessage sendMessage(Long senderId, Long receiverId, Long sessionId, String content, Integer messageType) {
        // 内容过滤
        String filteredContent = contentFilterService.filter(content);

        if (sessionId == null) {
            ChatSession session = chatSessionRepository.findByUser1IdAndUser2Id(senderId, receiverId)
                    .orElse(null);
            if (session == null) {
                session = chatSessionRepository.findByUser1IdAndUser2Id(receiverId, senderId).orElse(null);
            }
            if (session == null) {
                session = new ChatSession();
                session.setUser1Id(senderId);
                session.setUser2Id(receiverId);
                session = chatSessionRepository.save(session);
            }
            sessionId = session.getId();
        }

        ChatMessage message = new ChatMessage();
        message.setSessionId(sessionId);
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(filteredContent);
        message.setMessageType(messageType != null ? messageType : 1);
        message = chatMessageRepository.save(message);

        ChatSession session = chatSessionRepository.findById(sessionId).orElse(null);
        if (session != null) {
            session.setLastMessage(filteredContent);
            session.setLastMessageTime(LocalDateTime.now());
            chatSessionRepository.save(session);
        }

        return message;
    }

    /**
     * 发送消息降级方法 - 熔断器打开时调用
     */
    private ChatMessage sendMessageFallback(Long senderId, Long receiverId, Long sessionId, String content, Integer messageType, Exception e) {
        logger.error("聊天服务熔断降级: senderId={}, receiverId={}, error={}", senderId, receiverId, e.getMessage());
        ChatMessage fallbackMessage = new ChatMessage();
        fallbackMessage.setContent("消息发送服务暂时不可用，请稍后重试");
        fallbackMessage.setSenderId(senderId);
        fallbackMessage.setReceiverId(receiverId);
        fallbackMessage.setMessageType(0);
        return fallbackMessage;
    }

    /**
     * 发送消息限流降级方法
     */
    private ChatMessage sendMessageRateLimitFallback(Long senderId, Long receiverId, Long sessionId, String content, Integer messageType, Exception e) {
        logger.warn("聊天服务触发限流: senderId={}, receiverId={}", senderId, receiverId);
        ChatMessage fallbackMessage = new ChatMessage();
        fallbackMessage.setContent("发送频率过快，请稍后再试");
        fallbackMessage.setSenderId(senderId);
        fallbackMessage.setReceiverId(receiverId);
        fallbackMessage.setMessageType(0);
        return fallbackMessage;
    }

    @Transactional
    public void withdrawMessage(Long userId, Long messageId) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("消息不存在"));

        if (!message.getSenderId().equals(userId)) {
            throw new RuntimeException("只能撤回自己发送的消息");
        }

        if (message.getIsWithdraw() == 1) {
            throw new RuntimeException("消息已撤回");
        }

        message.setIsWithdraw(1);
        message.setContent("消息已撤回");
        chatMessageRepository.save(message);
    }

    @Transactional
    public void markAsRead(Long userId, Long sessionId) {
        List<ChatMessage> messages = chatMessageRepository.findBySessionIdOrderBySendTimeDesc(sessionId);
        for (ChatMessage message : messages) {
            if (message.getReceiverId().equals(userId) && message.getIsRead() == 0) {
                message.setIsRead(1);
                chatMessageRepository.save(message);
            }
        }
    }

    public long getUnreadCount(Long userId) {
        return chatMessageRepository.countByReceiverIdAndIsRead(userId, 0);
    }

    public List<String> getIcebreakers() {
        return Arrays.asList(
                "你好，看了你的资料感觉挺有眼缘的，想认识一下~",
                "嗨，你的兴趣爱好和我好像，你平时都喜欢做什么呀？",
                "最近有什么好玩的事情吗？想听听你的分享~",
                "周末一般怎么安排呀？有什么推荐的好去处吗？",
                "你理想中的另一半是什么样子的？"
        );
    }
}
