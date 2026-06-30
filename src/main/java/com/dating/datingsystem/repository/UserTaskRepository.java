package com.dating.datingsystem.repository;

import com.dating.datingsystem.entity.UserTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserTaskRepository extends JpaRepository<UserTask, Long>, JpaSpecificationExecutor<UserTask> {
    List<UserTask> findByUserIdAndTaskDate(Long userId, LocalDate taskDate);
    Optional<UserTask> findByUserIdAndTaskIdAndTaskDate(Long userId, Long taskId, LocalDate taskDate);
}
