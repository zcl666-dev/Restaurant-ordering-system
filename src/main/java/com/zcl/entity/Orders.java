package com.zcl.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单主表实体类
 */
@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_no", unique = true, nullable = false, length = 50)
    private String orderNo; // 订单号

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "cart_id")
    private Cart cart; // 来源购物车ID

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO; // 订单总金额

    @Column(name = "discount_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO; // 优惠金额

    @Column(name = "pay_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal payAmount = BigDecimal.ZERO; // 实付金额

    @Column(name = "points_earned", nullable = false)
    private Integer pointsEarned = 0; // 获得积分

    @Column(name = "dining_type", length = 20)
    private String diningType; // 堂食/外带

    @Column(name = "table_number", length = 20)
    private String tableNumber; // 桌号

    @Column(name = "order_status", nullable = false)
    private Integer orderStatus = 0; // 0待支付 1已支付 2制作中 3待取餐 4已完成 5已取消 6已退款

    @Column(name = "payment_status", nullable = false)
    private Integer paymentStatus = 0; // 0未支付 1已支付 2已退款

    @Column(name = "payment_method", length = 50)
    private String paymentMethod; // 支付方式

    @Column(name = "payment_time")
    private LocalDateTime paymentTime; // 支付时间

    @Column(name = "remark", length = 500)
    private String remark; // 订单备注

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
