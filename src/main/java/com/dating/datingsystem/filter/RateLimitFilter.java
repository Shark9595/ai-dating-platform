package com.dating.datingsystem.filter;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 接口限流过滤器
 * 基于令牌桶算法实现简单限流
 */
@Component
@Order(2)
public class RateLimitFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitFilter.class);

    private static final int MAX_REQUESTS_PER_MINUTE = 100;

    private static final Map<String, RateLimitInfo> rateLimitMap = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String clientIp = getClientIp(httpRequest);
        String uri = httpRequest.getRequestURI();

        if (uri.startsWith("/h2-console") || uri.startsWith("/actuator")) {
            chain.doFilter(request, response);
            return;
        }

        String key = clientIp + ":" + uri;
        RateLimitInfo info = rateLimitMap.computeIfAbsent(key, k -> new RateLimitInfo());

        synchronized (info) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - info.lastResetTime > 60000) {
                info.counter.set(0);
                info.lastResetTime = currentTime;
            }

            if (info.counter.incrementAndGet() > MAX_REQUESTS_PER_MINUTE) {
                logger.warn("接口限流触发, ip={}, uri={}, count={}", clientIp, uri, info.counter.get());
                sendRateLimitError(httpResponse);
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private void sendRateLimitError(HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(429);
        try (PrintWriter writer = response.getWriter()) {
            writer.write(JSON.toJSONString(Map.of(
                    "code", 429,
                    "message", "请求过于频繁，请稍后重试"
            )));
        }
    }

    @Override
    public void init(FilterConfig filterConfig) {}

    @Override
    public void destroy() {}

    private static class RateLimitInfo {
        AtomicInteger counter = new AtomicInteger(0);
        long lastResetTime = System.currentTimeMillis();
    }
}
