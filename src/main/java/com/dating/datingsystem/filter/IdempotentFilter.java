package com.dating.datingsystem.filter;

import com.alibaba.fastjson.JSON;
import com.dating.datingsystem.entity.IdempotentToken;
import com.dating.datingsystem.repository.IdempotentTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 幂等性过滤器
 * 处理需要幂等性保证的接口（如支付、订单等）
 */
@Component
@Order(1)
public class IdempotentFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(IdempotentFilter.class);

    @Autowired
    private IdempotentTokenRepository tokenRepository;

    private static final String IDEMPOTENT_HEADER = "Idempotent-Token";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String uri = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();

        if (!requiresIdempotency(uri, method)) {
            chain.doFilter(request, response);
            return;
        }

        String token = httpRequest.getHeader(IDEMPOTENT_HEADER);

        if (token == null || token.isEmpty()) {
            logger.warn("幂等性令牌为空, uri={}, method={}", uri, method);
            sendError(httpResponse, 400, "请提供幂等性令牌");
            return;
        }

        try {
            IdempotentToken existingToken = tokenRepository.findByToken(token).orElse(null);

            if (existingToken == null) {
                logger.info("生成新的幂等性令牌, token={}", token);
                IdempotentToken newToken = new IdempotentToken(
                        token,
                        null,
                        uri,
                        method,
                        0,
                        LocalDateTime.now(),
                        LocalDateTime.now().plusMinutes(5)
                );
                tokenRepository.save(newToken);
                chain.doFilter(request, response);
            } else {
                if (existingToken.getExpireTime().isBefore(LocalDateTime.now())) {
                    logger.warn("幂等性令牌已过期, token={}", token);
                    sendError(httpResponse, 400, "令牌已过期");
                    return;
                }

                if ("GET".equalsIgnoreCase(method)) {
                    chain.doFilter(request, response);
                    return;
                }

                if (existingToken.getStatus() == 1) {
                    logger.info("重复请求, 返回缓存结果, token={}", token);
                    httpResponse.setContentType("application/json;charset=UTF-8");
                    httpResponse.setStatus(200);
                    try (PrintWriter writer = httpResponse.getWriter()) {
                        writer.write(existingToken.getResult());
                    }
                    return;
                }

                chain.doFilter(request, response);
            }
        } catch (Exception e) {
            logger.error("幂等性校验异常, token={}", token, e);
            sendError(httpResponse, 500, "系统繁忙");
        }
    }

    private boolean requiresIdempotency(String uri, String method) {
        if (!"POST".equalsIgnoreCase(method)) {
            return false;
        }

        return uri.contains("/api/message/send") ||
               uri.contains("/api/post/create") ||
               uri.contains("/api/matchmaker/order") ||
               uri.contains("/api/vip/purchase") ||
               uri.contains("/api/vip/order") ||
               uri.contains("/api/vip/gift/send") ||
               uri.contains("/api/activity/register");
    }

    private void sendError(HttpServletResponse response, int status, String message) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(status);
        Map<String, Object> error = new HashMap<>();
        error.put("code", status);
        error.put("message", message);
        try (PrintWriter writer = response.getWriter()) {
            writer.write(JSON.toJSONString(error));
        }
    }

    @Override
    public void init(FilterConfig filterConfig) {}

    @Override
    public void destroy() {}
}
