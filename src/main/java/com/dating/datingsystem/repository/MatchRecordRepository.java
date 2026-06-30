package com.dating.datingsystem.repository;

import com.dating.datingsystem.entity.MatchRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchRecordRepository extends JpaRepository<MatchRecord, Long>, JpaSpecificationExecutor<MatchRecord> {
    List<MatchRecord> findByUserIdAndActionType(Long userId, Integer actionType);
    Optional<MatchRecord> findByUserIdAndTargetUserIdAndActionType(Long userId, Long targetUserId, Integer actionType);
    boolean existsByUserIdAndTargetUserIdAndActionType(Long userId, Long targetUserId, Integer actionType);
    long countByUserIdAndActionType(Long userId, Integer actionType);
}
