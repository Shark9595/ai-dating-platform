package com.dating.datingsystem.entity;

import lombok.Data;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "order_info")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 50)
    private String orderNo;

    private Long userId;

    private Integer orderType;

    private Long productId;

    @Column(length = 100)
    private String productName;

    private BigDecimal amount;

    private BigDecimal payAmount;

    private Integer payType;

    private Integer payStatus;

    private LocalDateTime payTime;

    private Integer status;

    @Column(length = 500)
    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
        if (status == null) status = 1;
        if (payStatus == null) payStatus = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
