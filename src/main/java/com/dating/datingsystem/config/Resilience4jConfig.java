package com.dating.datingsystem.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;

import java.time.Duration;

/**
 * Resilience4j 配置类
 * 提供熔断器、重试、限流器的自定义配置
 */
@Configuration
public class Resilience4jConfig {

    private static final Logger logger = LoggerFactory.getLogger(Resilience4jConfig.class);

    /**
     * 自定义 CircuitBreaker 配置
     */
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.ofDefaults();

        // 监听熔断器状态变化
        registry.getEventPublisher()
                .onEvent(event -> logger.info("CircuitBreaker Event: {}", event.toString()));

        logger.info("CircuitBreaker Registry initialized");
        return registry;
    }

    /**
     * 自定义 Retry 配置
     */
    @Bean
    public RetryRegistry retryRegistry() {
        RetryRegistry registry = RetryRegistry.ofDefaults();

        // 监听重试事件
        registry.getEventPublisher()
                .onEvent(event -> logger.info("Retry Event: {}", event.toString()));

        logger.info("Retry Registry initialized");
        return registry;
    }

    /**
     * 自定义 RateLimiter 配置
     */
    @Bean
    public RateLimiterRegistry rateLimiterRegistry() {
        RateLimiterRegistry registry = RateLimiterRegistry.ofDefaults();

        // 监听限流事件
        registry.getEventPublisher()
                .onEvent(event -> logger.info("RateLimiter Event: {}", event.toString()));

        logger.info("RateLimiter Registry initialized");
        return registry;
    }

    /**
     * 状态监控 - 熔断器状态检查
     */
    public void monitorCircuitBreakerState(String circuitBreakerName, CircuitBreakerRegistry registry) {
        CircuitBreaker circuitBreaker = registry.circuitBreaker(circuitBreakerName);
        CircuitBreaker.State state = circuitBreaker.getState();

        logger.info("CircuitBreaker '{}' state: {}", circuitBreakerName, state);

        // 添加状态转换监听
        circuitBreaker.getEventPublisher()
                .onStateTransition(event -> {
                    logger.warn("CircuitBreaker '{}' state transition: {} -> {}",
                            circuitBreakerName,
                            event.getStateTransition().getFromState(),
                            event.getStateTransition().getToState());
                });
    }
}