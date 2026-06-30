package com.dating.datingsystem.repository;

import com.dating.datingsystem.entity.PointsRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointsRecordRepository extends JpaRepository<PointsRecord, Long>, JpaSpecificationExecutor<PointsRecord> {
    List<PointsRecord> findByUserIdOrderByCreateTimeDesc(Long userId);
}
