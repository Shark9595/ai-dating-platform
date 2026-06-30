package com.dating.datingsystem.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dating.datingsystem.entity.Conversation;
import com.dating.datingsystem.entity.Message;
import com.dating.datingsystem.entity.User;
import com.dating.datingsystem.repository.ConversationRepository;
import com.dating.datingsystem.repository.MessageRepository;
import com.dating.datingsystem.repository.UserRepository;
import com.dating.datingsystem.service.AuditLogService;
import com.dating.datingsystem.service.ContentFilterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;

/**
 * 聊天WebSocket处理器
 */
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(ChatWebSocketHandler.class);

    /**
     * 存储在线用户的WebSocket会话，key为用户ID
     */
    private static final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContentFilterService contentFilterService;
    
    @Autowired
    private AuditLogService auditLogService;

    /**
     * 连接建立后调用
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            userSessions.put(userId, session);
            logger.info("用户 {} 建立WebSocket连接", userId);
        }
    }

    /**
     * 连接关闭后调用
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            userSessions.remove(userId);
            logger.info("用户 {} 断开WebSocket连接", userId);
        }
    }

    /**
     * 接收消息
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        JSONObject json = JSON.parseObject(payload);
        String type = json.getString("type");

        switch (type) {
            case "chat":
                handleChatMessage(session, json);
                break;
            case "heartbeat":
                handleHeartbeat(session);
                break;
            case "typing":
                handleTyping(session, json);
                break;
            default:
                logger.warn("未知的消息类型: {}", type);
        }
    }

    /**
     * 处理聊天消息
     */
    private void handleChatMessage(WebSocketSession session, JSONObject json) {
        try {
            Long senderId = getUserIdFromSession(session);
            if (senderId == null) {
                sendError(session, "用户未登录");
                return;
            }

            Long receiverId = json.getLong("receiverId");
            String content = json.getString("content");
            String messageType = json.getString("messageType");

            if (receiverId == null || content == null || content.trim().isEmpty()) {
                sendError(session, "消息参数不完整");
                return;
            }

            // 敏感词过滤
            String filteredContent = contentFilterService.filter(content);
            boolean hasSensitiveWord = !filteredContent.equals(content);
            
            if (hasSensitiveWord) {
                logger.info("用户 {} 发送的消息包含敏感词，已过滤", senderId);
                // 记录敏感词拦截日志
                Set<String> blockedWords = contentFilterService.findSensitiveWords(content);
                auditLogService.logSensitiveWordBlocked(senderId, content, List.copyOf(blockedWords));
            }

            // 获取或创建会话
            Conversation conversation = getOrCreateConversation(senderId, receiverId);

            // 保存消息
            Message msg = new Message();
            msg.setSenderId(senderId);
            msg.setReceiverId(receiverId);
            msg.setContent(filteredContent);
            msg.setConversationId(conversation.getId());
            msg.setType(parseMessageType(messageType));
            msg = messageRepository.save(msg);

            // 更新会话信息
            conversation.setLastMessageId(msg.getId());
            conversation.setLastMessageTime(LocalDateTime.now());
            // 增加接收者的未读数
            if (conversation.getUserId1().equals(receiverId)) {
                conversation.setUnreadCount1(conversation.getUnreadCount1() + 1);
            } else {
                conversation.setUnreadCount2(conversation.getUnreadCount2() + 1);
            }
            conversationRepository.save(conversation);

            // 记录消息发送日志
            auditLogService.logMessageSent(senderId, msg.getId(), filteredContent, 
                hasSensitiveWord ? AuditLogService.STATUS_BLOCKED : AuditLogService.STATUS_SUCCESS);

            // 构建响应消息
            JSONObject response = new JSONObject();
            response.put("type", "chat");
            response.put("messageId", msg.getId());
            response.put("senderId", senderId);
            response.put("receiverId", receiverId);
            response.put("content", filteredContent);
            response.put("messageType", msg.getType().name());
            response.put("conversationId", conversation.getId());
            response.put("createTime", msg.getCreateTime().toString());
            response.put("status", "success");
            if (hasSensitiveWord) {
                response.put("filtered", true);
            }

            // 发送给发送者确认
            sendMessage(session, response);

            // 发送给接收者（如果在线）
            WebSocketSession receiverSession = userSessions.get(receiverId);
            if (receiverSession != null && receiverSession.isOpen()) {
                sendMessage(receiverSession, response);
            }

        } catch (Exception e) {
            logger.error("处理聊天消息失败", e);
            sendError(session, "消息发送失败: " + e.getMessage());
        }
    }

    /**
     * 处理心跳
     */
    private void handleHeartbeat(WebSocketSession session) {
        JSONObject response = new JSONObject();
        response.put("type", "heartbeat");
        response.put("timestamp", System.currentTimeMillis());
        sendMessage(session, response);
    }

    /**
     * 处理正在输入状态
     */
    private void handleTyping(WebSocketSession session, JSONObject json) {
        Long senderId = getUserIdFromSession(session);
        Long receiverId = json.getLong("receiverId");

        if (senderId != null && receiverId != null) {
            WebSocketSession receiverSession = userSessions.get(receiverId);
            if (receiverSession != null && receiverSession.isOpen()) {
                JSONObject response = new JSONObject();
                response.put("type", "typing");
                response.put("senderId", senderId);
                sendMessage(receiverSession, response);
            }
        }
    }

    /**
     * 获取或创建会话
     */
    private Conversation getOrCreateConversation(Long userId1, Long userId2) {
        Conversation conversation = conversationRepository.findByUsers(userId1, userId2).orElse(null);
        if (conversation == null) {
            conversation = new Conversation();
            // 确保userId1小于userId2，避免重复创建会话
            if (userId1 < userId2) {
                conversation.setUserId1(userId1);
                conversation.setUserId2(userId2);
            } else {
                conversation.setUserId1(userId2);
                conversation.setUserId2(userId1);
            }
            conversation = conversationRepository.save(conversation);
        }
        return conversation;
    }

    /**
     * 解析消息类型
     */
    private Message.MessageType parseMessageType(String type) {
        if (type == null) {
            return Message.MessageType.TEXT;
        }
        try {
            return Message.MessageType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Message.MessageType.TEXT;
        }
    }

    /**
     * 从Session中获取用户ID
     */
    private Long getUserIdFromSession(WebSocketSession session) {
        // 从URL参数或Header中获取token，然后解析用户ID
        // 这里简化处理，从URL参数获取userId
        String query = session.getUri().getQuery();
        if (query != null && query.contains("userId=")) {
            String userIdStr = query.split("userId=")[1].split("&")[0];
            return Long.parseLong(userIdStr);
        }
        // 也可以从Header获取token解析
        String token = session.getHandshakeHeaders().getFirst("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            // 这里需要解析JWT token获取用户ID
            // 简化处理，实际项目中应该解析JWT
        }
        return null;
    }

    /**
     * 发送消息
     */
    private void sendMessage(WebSocketSession session, Object message) {
        if (session != null && session.isOpen()) {
            try {
                String json = message instanceof String ? (String) message : JSON.toJSONString(message);
                session.sendMessage(new TextMessage(json));
            } catch (IOException e) {
                logger.error("发送WebSocket消息失败", e);
            }
        }
    }

    /**
     * 发送错误消息
     */
    private void sendError(WebSocketSession session, String errorMsg) {
        JSONObject response = new JSONObject();
        response.put("type", "error");
        response.put("message", errorMsg);
        sendMessage(session, response);
    }

    /**
     * 向指定用户发送消息
     */
    public void sendMessageToUser(Long userId, Object message) {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            sendMessage(session, message);
        }
    }

    /**
     * 检查用户是否在线
     */
    public boolean isUserOnline(Long userId) {
        WebSocketSession session = userSessions.get(userId);
        return session != null && session.isOpen();
    }

    /**
     * 传输错误时调用
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            userSessions.remove(userId);
        }
        logger.error("WebSocket传输错误", exception);
    }
}