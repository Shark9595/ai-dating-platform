package com.dating.datingsystem.repository;

import com.dating.datingsystem.entity.Appeal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 申诉仓库
 */
@Repository
public interface AppealRepository extends JpaRepository<Appeal, Long> {

    List<Appeal> findByUserIdOrderByCreateTimeDesc(Long userId);

    List<Appeal> findByStatusOrderByCreateTimeDesc(Integer status);

    List<Appeal> findByStatus(Integer status);
}
