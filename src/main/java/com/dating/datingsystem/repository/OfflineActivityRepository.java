package com.dating.datingsystem.repository;

import com.dating.datingsystem.entity.OfflineActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfflineActivityRepository extends JpaRepository<OfflineActivity, Long>, JpaSpecificationExecutor<OfflineActivity> {
    List<OfflineActivity> findByStatusOrderByCreateTimeDesc(Integer status);
}
