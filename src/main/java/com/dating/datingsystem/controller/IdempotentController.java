package com.dating.datingsystem.controller;

import com.dating.datingsystem.common.Result;
import com.dating.datingsystem.entity.IdempotentToken;
import com.dating.datingsystem.repository.IdempotentTokenRepository;
import com.dating.datingsystem.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 幂等性令牌控制器
 */
@RestController
@RequestMapping("/api/idempotent")
public class IdempotentController {

    @Autowired
    private IdempotentTokenRepository tokenRepository;

    @Autowired
    private SecurityUtil securityUtil;

    /**
     * 获取幂等性令牌
     */
    @GetMapping("/token")
    public Result<Map<String, Object>> getToken() {
        Long userId = securityUtil.getCurrentUserId();
        
        String token = UUID.randomUUID().toString().replace("-", "").substring(0, 32);
        
        IdempotentToken idempotentToken = new IdempotentToken(
                token,
                userId,
                "",
                "",
                0,
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(5)
        );
        tokenRepository.save(idempotentToken);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("expireTime", idempotentToken.getExpireTime());
        
        return Result.success(result);
    }
}
