package com.dating.datingsystem.repository;

import com.dating.datingsystem.entity.DailyTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DailyTaskRepository extends JpaRepository<DailyTask, Long>, JpaSpecificationExecutor<DailyTask> {
    List<DailyTask> findByStatusOrderBySortAsc(Integer status);
}
