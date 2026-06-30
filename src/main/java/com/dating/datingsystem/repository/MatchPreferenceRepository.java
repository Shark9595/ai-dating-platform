package com.dating.datingsystem.repository;

import com.dating.datingsystem.entity.MatchPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MatchPreferenceRepository extends JpaRepository<MatchPreference, Long>, JpaSpecificationExecutor<MatchPreference> {
    Optional<MatchPreference> findByUserId(Long userId);
}
