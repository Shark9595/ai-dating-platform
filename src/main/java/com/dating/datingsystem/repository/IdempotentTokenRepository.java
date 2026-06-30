package com.dating.datingsystem.repository;

import com.dating.datingsystem.entity.IdempotentToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 幂等性令牌仓库
 */
@Repository
public interface IdempotentTokenRepository extends JpaRepository<IdempotentToken, Long> {

    Optional<IdempotentToken> findByToken(String token);

    @Modifying
    @Query("UPDATE IdempotentToken t SET t.status = :status, t.result = :result WHERE t.token = :token")
    int updateStatusAndResult(@Param("token") String token, @Param("status") Integer status, @Param("result") String result);

    @Modifying
    @Query("DELETE FROM IdempotentToken t WHERE t.expireTime < :time")
    void deleteExpiredTokens(@Param("time") LocalDateTime time);
}
