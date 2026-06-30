package com.dating.datingsystem.service;

import com.dating.datingsystem.entity.*;
import com.dating.datingsystem.repository.*;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class VipService {

    private static final Logger logger = LoggerFactory.getLogger(VipService.class);

    @Autowired
    private VipPackageRepository vipPackageRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VirtualGiftRepository virtualGiftRepository;

    @Autowired
    private GiftRecordRepository giftRecordRepository;
    
    @Autowired
    private AuditLogService auditLogService;

    public List<VipPackage> getVipPackages() {
        return vipPackageRepository.findByStatusOrderBySortAsc(1);
    }

    @Transactional
    @CircuitBreaker(name = "vip", fallbackMethod = "createVipOrderFallback")
    public Order createVipOrder(Long userId, Long packageId, Integer payType) {
        VipPackage vipPackage = vipPackageRepository.findById(packageId)
                .orElseThrow(() -> new RuntimeException("套餐不存在"));

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setOrderType(1);
        order.setProductId(packageId);
        order.setProductName(vipPackage.getName());
        order.setAmount(vipPackage.getPrice());
        order.setPayAmount(vipPackage.getPrice());
        order.setPayType(payType);
        Order savedOrder = orderRepository.save(order);
        
        // 记录VIP购买订单创建日志
        auditLogService.logVipPurchase(userId, packageId, AuditLogService.STATUS_SUCCESS);
        
        return savedOrder;
    }

    @Transactional
    @Retry(name = "vip-retry", fallbackMethod = "payOrderFallback")
    public void payOrder(Long userId, String orderNo) {
        logger.info("尝试支付订单: userId={}, orderNo={}", userId, orderNo);

        Order order = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("订单不属于当前用户");
        }

        if (order.getPayStatus() == 1) {
            throw new RuntimeException("订单已支付");
        }

        order.setPayStatus(1);
        order.setPayTime(LocalDateTime.now());
        orderRepository.save(order);

        if (order.getOrderType() == 1) {
            activateVip(userId, order.getProductId());
        }

        logger.info("订单支付成功: orderNo={}", orderNo);
    }

    @Transactional
    @Retry(name = "vip-retry", fallbackMethod = "activateVipFallback")
    public void activateVip(Long userId, Long packageId) {
        logger.info("激活VIP: userId={}, packageId={}", userId, packageId);

        VipPackage vipPackage = vipPackageRepository.findById(packageId)
                .orElseThrow(() -> new RuntimeException("套餐不存在"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireTime = user.getVipExpireTime();

        if (expireTime == null || expireTime.isBefore(now)) {
            expireTime = now.plusDays(vipPackage.getDurationDays());
        } else {
            expireTime = expireTime.plusDays(vipPackage.getDurationDays());
        }

        user.setVipLevel(vipPackage.getVipLevel());
        user.setVipExpireTime(expireTime);
        userRepository.save(user);

        logger.info("VIP激活成功: userId={}, vipLevel={}, expireTime={}", userId, vipPackage.getVipLevel(), expireTime);
    }

    /**
     * 创建VIP订单降级方法
     */
    private Order createVipOrderFallback(Long userId, Long packageId, Integer payType, Exception e) {
        logger.error("创建VIP订单降级: userId={}, packageId={}, error={}", userId, packageId, e.getMessage());
        throw new RuntimeException("VIP购买服务暂时不可用，请稍后重试");
    }

    /**
     * 支付订单降级方法
     */
    private void payOrderFallback(Long userId, String orderNo, Exception e) {
        logger.error("支付订单降级: userId={}, orderNo={}, error={}", userId, orderNo, e.getMessage());
        throw new RuntimeException("支付服务暂时不可用，请稍后重试或联系客服");
    }

    /**
     * 激活VIP降级方法
     */
    private void activateVipFallback(Long userId, Long packageId, Exception e) {
        logger.error("激活VIP降级: userId={}, packageId={}, error={}", userId, packageId, e.getMessage());
        throw new RuntimeException("VIP激活失败，请联系客服处理");
    }

    public List<Order> getMyOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreateTimeDesc(userId);
    }

    public Order getOrderDetail(String orderNo) {
        return orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new RuntimeException("订单不存在"));
    }

    public List<VirtualGift> getVirtualGifts() {
        return virtualGiftRepository.findByStatusOrderBySortAsc(1);
    }

    @Transactional
    public GiftRecord sendGift(Long senderId, Long receiverId, Long giftId, Integer quantity, Long sessionId) {
        VirtualGift gift = virtualGiftRepository.findById(giftId)
                .orElseThrow(() -> new RuntimeException("礼物不存在"));

        GiftRecord record = new GiftRecord();
        record.setSenderId(senderId);
        record.setReceiverId(receiverId);
        record.setGiftId(giftId);
        record.setGiftName(gift.getName());
        record.setQuantity(quantity != null ? quantity : 1);
        record.setSessionId(sessionId);
        GiftRecord savedRecord = giftRecordRepository.save(record);
        
        // 记录礼物发送日志
        auditLogService.logGiftSent(senderId, giftId, receiverId, quantity != null ? quantity : 1, AuditLogService.STATUS_SUCCESS);
        
        return savedRecord;
    }

    public List<Map<String, Object>> getGiftRecords(Long userId, int page, int size) {
        List<GiftRecord> allRecords = giftRecordRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (GiftRecord record : allRecords) {
            if (record.getSenderId().equals(userId) || record.getReceiverId().equals(userId)) {
                Map<String, Object> item = new HashMap<>();
                item.put("record", record);
                User sender = userRepository.findById(record.getSenderId()).orElse(null);
                User receiver = userRepository.findById(record.getReceiverId()).orElse(null);
                item.put("sender", sender);
                item.put("receiver", receiver);
                result.add(item);
            }
        }
        int start = page * size;
        int end = Math.min(start + size, result.size());
        if (start >= result.size()) {
            return new ArrayList<>();
        }
        return result.subList(start, end);
    }

    @Transactional
    public void refundApply(Long userId, Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("订单不属于当前用户");
        }

        order.setStatus(2);
        order.setRemark(reason);
        orderRepository.save(order);
    }

    private String generateOrderNo() {
        return "VIP" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public boolean isVip(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getVipLevel() == null || user.getVipLevel() == 0) {
            return false;
        }
        if (user.getVipExpireTime() == null || user.getVipExpireTime().isBefore(LocalDateTime.now())) {
            return false;
        }
        return true;
    }
}
