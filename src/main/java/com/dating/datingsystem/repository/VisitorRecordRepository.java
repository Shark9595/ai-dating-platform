package com.dating.datingsystem.repository;

import com.dating.datingsystem.entity.VisitorRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VisitorRecordRepository extends JpaRepository<VisitorRecord, Long>, JpaSpecificationExecutor<VisitorRecord> {
    List<VisitorRecord> findByVisitedIdAndIsAnonymousOrderByVisitTimeDesc(Long visitedId, Integer isAnonymous);
    long countByVisitedId(Long visitedId);
}
