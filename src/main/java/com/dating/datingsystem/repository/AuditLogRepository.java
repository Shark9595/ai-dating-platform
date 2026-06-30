package com.dating.datingsystem.repository;

import com.dating.datingsystem.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审计日志Repository
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * 按用户ID查询审计日志
     */
    Page<AuditLog> findByUserId(Long userId, Pageable pageable);

    /**
     * 按操作类型查询审计日志
     */
    Page<AuditLog> findByAction(String action, Pageable pageable);

    /**
     * 按用户ID和操作类型查询
     */
    Page<AuditLog> findByUserIdAndAction(Long userId, String action, Pageable pageable);

    /**
     * 按状态查询
     */
    Page<AuditLog> findByStatus(String status, Pageable pageable);

    /**
     * 按时间范围查询
     */
    Page<AuditLog> findByCreateTimeBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * 按用户ID和时间范围查询
     */
    Page<AuditLog> findByUserIdAndCreateTimeBetween(Long userId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * 按资源类型和资源ID查询
     */
    List<AuditLog> findByResourceTypeAndResourceId(String resourceType, Long resourceId);

    /**
     * 统计用户指定时间段内的操作次数
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.userId = :userId AND a.action = :action AND a.createTime >= :startTime")
    Long countUserActionSince(@Param("userId") Long userId, @Param("action") String action, @Param("startTime") LocalDateTime startTime);

    /**
     * 查询用户最近的操作记录
     */
    List<AuditLog> findTop10ByUserIdOrderByCreateTimeDesc(Long userId);

    /**
     * 查询指定IP地址的操作记录
     */
    Page<AuditLog> findByIpAddress(String ipAddress, Pageable pageable);

    /**
     * 统计指定时间段内的失败操作次数
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.status = 'FAILED' AND a.createTime BETWEEN :startTime AND :endTime")
    Long countFailedActionsBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 统计指定时间段内的被拦截操作次数
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.status = 'BLOCKED' AND a.createTime BETWEEN :startTime AND :endTime")
    Long countBlockedActionsBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}