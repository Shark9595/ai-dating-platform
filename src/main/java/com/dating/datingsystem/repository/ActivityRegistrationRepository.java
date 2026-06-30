package com.dating.datingsystem.repository;

import com.dating.datingsystem.entity.ActivityRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityRegistrationRepository extends JpaRepository<ActivityRegistration, Long>, JpaSpecificationExecutor<ActivityRegistration> {
    List<ActivityRegistration> findByUserIdOrderByCreateTimeDesc(Long userId);
    List<ActivityRegistration> findByActivityIdOrderByCreateTimeDesc(Long activityId);
    Optional<ActivityRegistration> findByActivityIdAndUserId(Long activityId, Long userId);
    boolean existsByActivityIdAndUserId(Long activityId, Long userId);
}
