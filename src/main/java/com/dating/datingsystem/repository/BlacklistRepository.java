package com.dating.datingsystem.repository;

import com.dating.datingsystem.entity.Blacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlacklistRepository extends JpaRepository<Blacklist, Long>, JpaSpecificationExecutor<Blacklist> {
    List<Blacklist> findByUserId(Long userId);
    Optional<Blacklist> findByUserIdAndBlackedUserId(Long userId, Long blackedUserId);
    boolean existsByUserIdAndBlackedUserId(Long userId, Long blackedUserId);
}
