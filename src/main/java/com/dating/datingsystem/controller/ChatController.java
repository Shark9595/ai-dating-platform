package com.dating.datingsystem.controller;

import com.dating.datingsystem.common.Result;
import com.dating.datingsystem.entity.ChatMessage;
import com.dating.datingsystem.service.ChatService;
import com.dating.datingsystem.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private SecurityUtil securityUtil;

    @GetMapping("/sessions")
    public Result<List<Map<String, Object>>> getChatSessions() {
        Long userId = securityUtil.getCurrentUserId();
        List<Map<String, Object>> list = chatService.getChatSessions(userId);
        return Result.success(list);
    }

    @GetMapping("/messages/{sessionId}")
    public Result<List<ChatMessage>> getChatMessages(
            @PathVariable Long sessionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<ChatMessage> list = chatService.getChatMessages(sessionId, page, size);
        return Result.success(list);
    }

    @PostMapping("/send")
    public Result<ChatMessage> sendMessage(@RequestBody Map<String, Object> params) {
        Long userId = securityUtil.getCurrentUserId();
        Long receiverId = Long.valueOf(params.get("receiverId").toString());
        Long sessionId = params.get("sessionId") != null ? Long.valueOf(params.get("sessionId").toString()) : null;
        String content = params.get("content").toString();
        Integer messageType = params.get("messageType") != null ? Integer.valueOf(params.get("messageType").toString()) : 1;
        ChatMessage message = chatService.sendMessage(userId, receiverId, sessionId, content, messageType);
        return Result.success("发送成功", message);
    }

    @PostMapping("/withdraw/{messageId}")
    public Result<Void> withdrawMessage(@PathVariable Long messageId) {
        Long userId = securityUtil.getCurrentUserId();
        chatService.withdrawMessage(userId, messageId);
        return Result.success("撤回成功");
    }

    @PostMapping("/read/{sessionId}")
    public Result<Void> markAsRead(@PathVariable Long sessionId) {
        Long userId = securityUtil.getCurrentUserId();
        chatService.markAsRead(userId, sessionId);
        return Result.success();
    }

    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount() {
        Long userId = securityUtil.getCurrentUserId();
        long count = chatService.getUnreadCount(userId);
        return Result.success(count);
    }

    @GetMapping("/icebreakers")
    public Result<List<String>> getIcebreakers() {
        List<String> list = chatService.getIcebreakers();
        return Result.success(list);
    }
}
