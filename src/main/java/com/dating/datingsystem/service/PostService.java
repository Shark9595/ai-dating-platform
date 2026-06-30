package com.dating.datingsystem.service;

import com.dating.datingsystem.entity.Post;
import com.dating.datingsystem.entity.PostComment;
import com.dating.datingsystem.entity.User;
import com.dating.datingsystem.repository.PostCommentRepository;
import com.dating.datingsystem.repository.PostRepository;
import com.dating.datingsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostCommentRepository postCommentRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AuditLogService auditLogService;

    public List<Map<String, Object>> getPostList(int page, int size) {
        List<Post> posts = postRepository.findByStatusAndAuditStatusOrderByCreateTimeDesc(1, 1);
        int start = page * size;
        int end = Math.min(start + size, posts.size());
        if (start >= posts.size()) {
            return new ArrayList<>();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Post post : posts.subList(start, end)) {
            Map<String, Object> item = new HashMap<>();
            // 扁平化数据结构
            item.put("id", post.getId());
            item.put("userId", post.getUserId());
            item.put("content", post.getContent());
            item.put("images", post.getImages());
            item.put("likeCount", post.getLikeCount());
            item.put("commentCount", post.getCommentCount());
            item.put("viewCount", post.getViewCount());
            item.put("createTime", post.getCreateTime());
            User user = userRepository.findById(post.getUserId()).orElse(null);
            item.put("user", user);
            result.add(item);
        }
        return result;
    }

    public List<Map<String, Object>> getUserPosts(Long userId) {
        List<Post> posts = postRepository.findByUserIdOrderByCreateTimeDesc(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Post post : posts) {
            Map<String, Object> item = new HashMap<>();
            item.put("post", post);
            User user = userRepository.findById(post.getUserId()).orElse(null);
            item.put("user", user);
            result.add(item);
        }
        return result;
    }

    public Map<String, Object> getPostDetail(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("动态不存在"));

        post.setViewCount(post.getViewCount() + 1);
        postRepository.save(post);

        Map<String, Object> result = new HashMap<>();
        result.put("post", post);
        User user = userRepository.findById(post.getUserId()).orElse(null);
        result.put("user", user);
        return result;
    }

    @Transactional
    public Post createPost(Long userId, String content, String images) {
        Post post = new Post();
        post.setUserId(userId);
        post.setContent(content);
        post.setImages(images);
        Post savedPost = postRepository.save(post);
        
        // 记录帖子创建日志
        auditLogService.logPostCreated(userId, savedPost.getId(), content);
        
        return savedPost;
    }

    @Transactional
    public void deletePost(Long userId, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("动态不存在"));

        if (!post.getUserId().equals(userId)) {
            throw new RuntimeException("只能删除自己的动态");
        }

        post.setStatus(0);
        postRepository.save(post);
        
        // 记录帖子删除日志
        auditLogService.logPostDeleted(userId, postId);
    }

    @Transactional
    public void likePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("动态不存在"));
        post.setLikeCount(post.getLikeCount() + 1);
        postRepository.save(post);
    }

    @Transactional
    public void unlikePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("动态不存在"));
        if (post.getLikeCount() > 0) {
            post.setLikeCount(post.getLikeCount() - 1);
            postRepository.save(post);
        }
    }

    public List<Map<String, Object>> getPostComments(Long postId) {
        List<PostComment> comments = postCommentRepository.findByPostIdOrderByCreateTimeDesc(postId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (PostComment comment : comments) {
            Map<String, Object> item = new HashMap<>();
            item.put("comment", comment);
            User user = userRepository.findById(comment.getUserId()).orElse(null);
            item.put("user", user);
            result.add(item);
        }
        return result;
    }

    @Transactional
    public PostComment addComment(Long userId, Long postId, String content, Long parentId, Long replyUserId) {
        PostComment comment = new PostComment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setParentId(parentId);
        comment.setReplyUserId(replyUserId);
        comment = postCommentRepository.save(comment);

        Post post = postRepository.findById(postId).orElse(null);
        if (post != null) {
            post.setCommentCount(post.getCommentCount() + 1);
            postRepository.save(post);
        }

        return comment;
    }

    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        PostComment comment = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("评论不存在"));

        if (!comment.getUserId().equals(userId)) {
            throw new RuntimeException("只能删除自己的评论");
        }

        comment.setStatus(0);
        postCommentRepository.save(comment);

        Post post = postRepository.findById(comment.getPostId()).orElse(null);
        if (post != null && post.getCommentCount() > 0) {
            post.setCommentCount(post.getCommentCount() - 1);
            postRepository.save(post);
        }
    }

    @Transactional
    public void likeComment(Long commentId) {
        PostComment comment = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("评论不存在"));
        comment.setLikeCount(comment.getLikeCount() + 1);
        postCommentRepository.save(comment);
    }

    public List<Map<String, Object>> getAuditList(int page, int size) {
        List<Post> posts = postRepository.findByAuditStatusOrderByCreateTimeDesc(0);
        int start = page * size;
        int end = Math.min(start + size, posts.size());
        if (start >= posts.size()) {
            return new ArrayList<>();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Post post : posts.subList(start, end)) {
            Map<String, Object> item = new HashMap<>();
            item.put("post", post);
            User user = userRepository.findById(post.getUserId()).orElse(null);
            item.put("user", user);
            result.add(item);
        }
        return result;
    }

    @Transactional
    public void approvePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("动态不存在"));
        post.setAuditStatus(1);
        post.setStatus(1);
        postRepository.save(post);
    }

    @Transactional
    public void rejectPost(Long postId, String remark) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("动态不存在"));
        post.setAuditStatus(2);
        post.setStatus(0);
        post.setAuditRemark(remark);
        postRepository.save(post);
    }
}
