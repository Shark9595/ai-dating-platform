package com.dating.datingsystem.controller;

import com.dating.datingsystem.common.Result;
import com.dating.datingsystem.entity.Post;
import com.dating.datingsystem.entity.PostComment;
import com.dating.datingsystem.service.DeepSeekService;
import com.dating.datingsystem.service.PostService;
import com.dating.datingsystem.utils.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/post")
public class PostController {

    private static final Logger logger = LoggerFactory.getLogger(PostController.class);

    @Autowired
    private PostService postService;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private DeepSeekService deepSeekService;

    /**
     * 使用 DeepSeek AI 生成动态内容建议
     */
    @GetMapping("/suggest-content")
    public Result<Map<String, String>> suggestContent() {
        String systemPrompt = "你是一个婚恋交友平台的内容策划专家，负责为用户提供有趣、真实、积极的动态内容建议。请生成2-3条适合在婚恋平台发布的动态，内容要：1) 真实自然，展现用户的生活和兴趣爱好；2) 积极向上，传递正能量；3) 容易引起共鸣和互动；4) 适合陌生人社交场景。回复格式：每条内容一行，不要编号，直接返回内容。";

        String userPrompt = "请生成一些有趣的动态内容建议，用于婚恋交友平台。";

        try {
            String content = deepSeekService.callDeepSeek(systemPrompt, userPrompt);
            String[] suggestions = content.split("\n");
            String randomSuggestion = suggestions[(int)(Math.random() * suggestions.length)].trim();
            if (randomSuggestion.isEmpty() && suggestions.length > 0) {
                randomSuggestion = suggestions[0].trim();
            }
            if (randomSuggestion.length() > 200) {
                randomSuggestion = randomSuggestion.substring(0, 200);
            }
            return Result.success(Map.of("content", randomSuggestion));
        } catch (Exception e) {
            logger.error("生成动态内容失败", e);
            // 返回默认内容
            String[] defaultContents = {
                "今天天气真好，出去走走~",
                "周末做了一顿大餐，有没有想尝尝的？",
                "新的一周，努力工作！",
                "读完了一本好书，推荐给大家",
                "今天尝试了一道新菜，味道还不错！",
                "周末约了朋友逛街，开心的一天~"
            };
            String randomDefault = defaultContents[(int)(Math.random() * defaultContents.length)];
            return Result.success(Map.of("content", randomDefault));
        }
    }

    @GetMapping("/list")
    public Result<List<Map<String, Object>>> getPostList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<Map<String, Object>> list = postService.getPostList(page, size);
        return Result.success(list);
    }

    @GetMapping("/user/{userId}")
    public Result<List<Map<String, Object>>> getUserPosts(@PathVariable Long userId) {
        List<Map<String, Object>> list = postService.getUserPosts(userId);
        return Result.success(list);
    }

    @GetMapping("/detail/{postId}")
    public Result<Map<String, Object>> getPostDetail(@PathVariable Long postId) {
        Map<String, Object> result = postService.getPostDetail(postId);
        return Result.success(result);
    }

    @PostMapping("/create")
    public Result<Post> createPost(@RequestBody Map<String, String> params) {
        Long userId = securityUtil.getCurrentUserId();
        Post post = postService.createPost(userId, params.get("content"), params.get("images"));
        return Result.success("发布成功", post);
    }

    @DeleteMapping("/{postId}")
    public Result<Void> deletePost(@PathVariable Long postId) {
        Long userId = securityUtil.getCurrentUserId();
        postService.deletePost(userId, postId);
        return Result.success("删除成功");
    }

    @PostMapping("/like/{postId}")
    public Result<Void> likePost(@PathVariable Long postId) {
        postService.likePost(postId);
        return Result.success();
    }

    @PostMapping("/unlike/{postId}")
    public Result<Void> unlikePost(@PathVariable Long postId) {
        postService.unlikePost(postId);
        return Result.success();
    }

    @GetMapping("/comments/{postId}")
    public Result<List<Map<String, Object>>> getPostComments(@PathVariable Long postId) {
        List<Map<String, Object>> list = postService.getPostComments(postId);
        return Result.success(list);
    }

    @PostMapping("/comment")
    public Result<PostComment> addComment(@RequestBody Map<String, Object> params) {
        Long userId = securityUtil.getCurrentUserId();
        Long postId = Long.valueOf(params.get("postId").toString());
        String content = params.get("content").toString();
        Long parentId = params.get("parentId") != null ? Long.valueOf(params.get("parentId").toString()) : null;
        Long replyUserId = params.get("replyUserId") != null ? Long.valueOf(params.get("replyUserId").toString()) : null;
        PostComment comment = postService.addComment(userId, postId, content, parentId, replyUserId);
        return Result.success("评论成功", comment);
    }

    @DeleteMapping("/comment/{commentId}")
    public Result<Void> deleteComment(@PathVariable Long commentId) {
        Long userId = securityUtil.getCurrentUserId();
        postService.deleteComment(userId, commentId);
        return Result.success("删除成功");
    }

    @PostMapping("/comment/like/{commentId}")
    public Result<Void> likeComment(@PathVariable Long commentId) {
        postService.likeComment(commentId);
        return Result.success();
    }

    /**
     * 获取待审核帖子列表（管理员）
     */
    @GetMapping("/audit/list")
    public Result<List<Map<String, Object>>> getAuditList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<Map<String, Object>> list = postService.getAuditList(page, size);
        return Result.success(list);
    }

    /**
     * 审核通过
     */
    @PostMapping("/audit/approve/{postId}")
    public Result<Void> approvePost(@PathVariable Long postId) {
        postService.approvePost(postId);
        return Result.success("审核通过");
    }

    /**
     * 审核拒绝
     */
    @PostMapping("/audit/reject/{postId}")
    public Result<Void> rejectPost(@PathVariable Long postId, @RequestBody Map<String, String> params) {
        String remark = params.get("remark");
        postService.rejectPost(postId, remark);
        return Result.success("审核拒绝");
    }
}
