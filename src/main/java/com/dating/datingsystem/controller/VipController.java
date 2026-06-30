package com.dating.datingsystem.controller;

import com.dating.datingsystem.common.Result;
import com.dating.datingsystem.entity.*;
import com.dating.datingsystem.service.VipService;
import com.dating.datingsystem.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vip")
public class VipController {

    @Autowired
    private VipService vipService;

    @Autowired
    private SecurityUtil securityUtil;

    @GetMapping("/packages")
    public Result<List<VipPackage>> getVipPackages() {
        List<VipPackage> list = vipService.getVipPackages();
        return Result.success(list);
    }

    @PostMapping("/order")
    public Result<Order> createVipOrder(@RequestBody Map<String, Object> params) {
        Long userId = securityUtil.getCurrentUserId();
        Long packageId = Long.valueOf(params.get("packageId").toString());
        Integer payType = params.get("payType") != null ? Integer.valueOf(params.get("payType").toString()) : 1;
        Order order = vipService.createVipOrder(userId, packageId, payType);
        return Result.success("订单创建成功", order);
    }

    @PostMapping("/pay/{orderNo}")
    public Result<Void> payOrder(@PathVariable String orderNo) {
        Long userId = securityUtil.getCurrentUserId();
        vipService.payOrder(userId, orderNo);
        return Result.success("支付成功");
    }

    @GetMapping("/orders")
    public Result<List<Order>> getMyOrders() {
        Long userId = securityUtil.getCurrentUserId();
        List<Order> list = vipService.getMyOrders(userId);
        return Result.success(list);
    }

    @GetMapping("/order/{orderNo}")
    public Result<Order> getOrderDetail(@PathVariable String orderNo) {
        Order order = vipService.getOrderDetail(orderNo);
        return Result.success(order);
    }

    @PostMapping("/refund/{orderId}")
    public Result<Void> refundApply(@PathVariable Long orderId, @RequestBody Map<String, String> params) {
        Long userId = securityUtil.getCurrentUserId();
        vipService.refundApply(userId, orderId, params.get("reason"));
        return Result.success("退款申请提交成功");
    }

    @GetMapping("/gifts")
    public Result<List<VirtualGift>> getVirtualGifts() {
        List<VirtualGift> list = vipService.getVirtualGifts();
        return Result.success(list);
    }

    @PostMapping("/gift/send")
    public Result<GiftRecord> sendGift(@RequestBody Map<String, Object> params) {
        Long userId = securityUtil.getCurrentUserId();
        Long receiverId = Long.valueOf(params.get("receiverId").toString());
        Long giftId = Long.valueOf(params.get("giftId").toString());
        Integer quantity = params.get("quantity") != null ? Integer.valueOf(params.get("quantity").toString()) : 1;
        Long sessionId = params.get("sessionId") != null ? Long.valueOf(params.get("sessionId").toString()) : null;
        GiftRecord record = vipService.sendGift(userId, receiverId, giftId, quantity, sessionId);
        return Result.success("赠送成功", record);
    }

    @GetMapping("/gift/records")
    public Result<List<Map<String, Object>>> getGiftRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = securityUtil.getCurrentUserId();
        List<Map<String, Object>> list = vipService.getGiftRecords(userId, page, size);
        return Result.success(list);
    }

    @GetMapping("/is-vip")
    public Result<Boolean> isVip() {
        Long userId = securityUtil.getCurrentUserId();
        boolean vip = vipService.isVip(userId);
        return Result.success(vip);
    }
}
