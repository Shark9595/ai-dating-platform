package com.dating.datingsystem.repository;

import com.dating.datingsystem.entity.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostCommentRepository extends JpaRepository<PostComment, Long>, JpaSpecificationExecutor<PostComment> {
    List<PostComment> findByPostIdOrderByCreateTimeDesc(Long postId);
}
