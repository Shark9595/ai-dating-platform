package com.dating.datingsystem.repository;

import com.dating.datingsystem.entity.ReportRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRecordRepository extends JpaRepository<ReportRecord, Long>, JpaSpecificationExecutor<ReportRecord> {
    List<ReportRecord> findByReporterIdOrderByCreateTimeDesc(Long reporterId);
    List<ReportRecord> findByStatusOrderByCreateTimeDesc(Integer status);
}
