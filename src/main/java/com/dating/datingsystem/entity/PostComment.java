package com.dating.datingsystem.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "post_comment")
public class PostComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long postId;

    private Long userId;

    private Long parentId;

    private Long replyUserId;

    @Column(columnDefinition = "TEXT")
    private String content;

    private Integer likeCount;

    private Integer status;

    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        if (likeCount == null) likeCount = 0;
        if (status == null) status = 1;
    }
}
