package com.dating.datingsystem.repository;

import com.dating.datingsystem.entity.MatchmakerServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchmakerServiceRepository extends JpaRepository<MatchmakerServiceEntity, Long>, JpaSpecificationExecutor<MatchmakerServiceEntity> {
    List<MatchmakerServiceEntity> findByUserIdOrderByCreateTimeDesc(Long userId);
    List<MatchmakerServiceEntity> findByMatchmakerIdOrderByCreateTimeDesc(Long matchmakerId);
}
