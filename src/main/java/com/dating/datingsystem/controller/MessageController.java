package com.dating.datingsystem.controller;

import com.dating.datingsystem.common.Result;
import com.dating.datingsystem.entity.Conversation;
import com.dating.datingsystem.entity.Message;
import com.dating.datingsystem.entity.User;
import com.dating.datingsystem.repository.ConversationRepository;
import com.dating.datingsystem.repository.MessageRepository;
import com.dating.datingsystem.repository.UserRepository;
import com.dating.datingsystem.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 消息控制器
 */
@RestController
@RequestMapping("/api/message")
public class MessageController {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SecurityUtil securityUtil;

    /**
     * 获取会话列表
     */
    @GetMapping("/conversations")
    public Result<List<Map<String, Object>>> getConversations() {
        Long userId = securityUtil.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }

        List<Conversation> conversations = conversationRepository.findByUserIdOrderByLastMessageTimeDesc(userId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Conversation conversation : conversations) {
            Map<String, Object> item = new HashMap<>();
            item.put("conversation", conversation);

            // 获取对方用户信息
            Long otherUserId = conversation.getUserId1().equals(userId) 
                    ? conversation.getUserId2() 
                    : conversation.getUserId1();
            User otherUser = userRepository.findById(otherUserId).orElse(null);
            item.put("otherUser", otherUser);

            // 获取未读数
            int unreadCount = conversation.getUserId1().equals(userId) 
                    ? conversation.getUnreadCount2() 
                    : conversation.getUnreadCount1();
            item.put("unreadCount", unreadCount);

            // 获取最后一条消息
            if (conversation.getLastMessageId() != null) {
                Message lastMessage = messageRepository.findById(conversation.getLastMessageId()).orElse(null);
                item.put("lastMessage", lastMessage);
            }

            result.add(item);
        }

        return Result.success(result);
    }

    /**
     * 获取聊天记录
     */
    @GetMapping("/history/{conversationId}")
    public Result<Map<String, Object>> getMessageHistory(
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Long userId = securityUtil.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }

        Conversation conversation = conversationRepository.findById(conversationId).orElse(null);
        if (conversation == null) {
            return Result.error(404, "会话不存在");
        }

        // 验证用户是否属于该会话
        if (!conversation.getUserId1().equals(userId) && !conversation.getUserId2().equals(userId)) {
            return Result.error(403, "无权访问该会话");
        }

        // 获取消息列表
        List<Message> messages = messageRepository.findByConversationIdOrderByCreateTimeDesc(conversationId);
        
        // 分页处理
        int start = page * size;
        int end = Math.min(start + size, messages.size());
        List<Message> pagedMessages = start < messages.size() 
                ? messages.subList(start, end) 
                : new ArrayList<>();

        // 反转消息顺序，使其按时间升序显示
        Collections.reverse(pagedMessages);

        Map<String, Object> result = new HashMap<>();
        result.put("messages", pagedMessages);
        result.put("total", messages.size());
        result.put("page", page);
        result.put("size", size);

        return Result.success(result);
    }

    /**
     * 获取未读消息数
     */
    @GetMapping("/unread")
    public Result<Map<String, Object>> getUnreadCount() {
        Long userId = securityUtil.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }

        long totalCount = messageRepository.countByReceiverIdAndStatus(userId, Message.MessageStatus.UNREAD);

        // 获取每个会话的未读数
        List<Conversation> conversations = conversationRepository.findByUserIdOrderByLastMessageTimeDesc(userId);
        List<Map<String, Object>> conversationUnreadList = new ArrayList<>();
        
        for (Conversation conversation : conversations) {
            int unreadCount;
            if (conversation.getUserId1().equals(userId)) {
                unreadCount = conversation.getUnreadCount2() != null ? conversation.getUnreadCount2() : 0;
            } else {
                unreadCount = conversation.getUnreadCount1() != null ? conversation.getUnreadCount1() : 0;
            }
            
            if (unreadCount > 0) {
                Map<String, Object> item = new HashMap<>();
                item.put("conversationId", conversation.getId());
                item.put("unreadCount", unreadCount);
                
                Long otherUserId = conversation.getUserId1().equals(userId) 
                        ? conversation.getUserId2() 
                        : conversation.getUserId1();
                item.put("otherUserId", otherUserId);
                
                conversationUnreadList.add(item);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalUnreadCount", totalCount);
        result.put("conversationUnreadList", conversationUnreadList);

        return Result.success(result);
    }

    /**
     * 标记消息已读
     */
    @PostMapping("/markRead")
    @Transactional
    public Result<Void> markAsRead(@RequestBody Map<String, Long> params) {
        Long userId = securityUtil.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }

        Long conversationId = params.get("conversationId");
        if (conversationId == null) {
            return Result.error(400, "会话ID不能为空");
        }

        Conversation conversation = conversationRepository.findById(conversationId).orElse(null);
        if (conversation == null) {
            return Result.error(404, "会话不存在");
        }

        // 验证用户是否属于该会话
        if (!conversation.getUserId1().equals(userId) && !conversation.getUserId2().equals(userId)) {
            return Result.error(403, "无权操作该会话");
        }

        // 标记消息为已读
        messageRepository.markAllAsRead(conversationId, userId, Message.MessageStatus.READ, Message.MessageStatus.UNREAD);

        // 重置会话中用户的未读计数
        if (conversation.getUserId1().equals(userId)) {
            conversation.setUnreadCount1(0);
        } else {
            conversation.setUnreadCount2(0);
        }
        conversationRepository.save(conversation);

        return Result.success("标记成功");
    }

    /**
     * 获取与指定用户的会话和消息
     */
    @GetMapping("/conversation/{otherUserId}")
    public Result<Map<String, Object>> getConversationWithUser(
            @PathVariable Long otherUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Long userId = securityUtil.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }

        // 查找或创建会话
        Conversation conversation = conversationRepository.findByUsers(userId, otherUserId).orElse(null);
        if (conversation == null) {
            // 创建新会话
            conversation = new Conversation();
            if (userId < otherUserId) {
                conversation.setUserId1(userId);
                conversation.setUserId2(otherUserId);
            } else {
                conversation.setUserId1(otherUserId);
                conversation.setUserId2(userId);
            }
            conversation = conversationRepository.save(conversation);
        }

        // 获取消息
        List<Message> messages = messageRepository.findByConversationIdOrderByCreateTimeDesc(conversation.getId());
        int start = page * size;
        int end = Math.min(start + size, messages.size());
        List<Message> pagedMessages = start < messages.size() 
                ? messages.subList(start, end) 
                : new ArrayList<>();

        Collections.reverse(pagedMessages);

        User otherUser = userRepository.findById(otherUserId).orElse(null);

        Map<String, Object> result = new HashMap<>();
        result.put("conversation", conversation);
        result.put("messages", pagedMessages);
        result.put("otherUser", otherUser);
        result.put("total", messages.size());

        return Result.success(result);
    }
}