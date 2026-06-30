package com.dating.datingsystem.repository;

import com.dating.datingsystem.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long>, JpaSpecificationExecutor<Post> {
    List<Post> findByUserIdOrderByCreateTimeDesc(Long userId);
    List<Post> findByStatusAndAuditStatusOrderByCreateTimeDesc(Integer status, Integer auditStatus);
    List<Post> findByAuditStatusOrderByCreateTimeDesc(Integer auditStatus);
}
