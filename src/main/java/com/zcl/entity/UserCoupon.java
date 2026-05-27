package com.zcl.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户兑换券实体类
 */
@Entity
@Table(name = "user_coupon")
@Data
@NoArgsConstructor
public class UserCoupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "mall_id", nullable = false)
    private PointsMall mall; // 积分商城ID

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "coupon_name_snapshot", nullable = false, length = 100)
    private String couponNameSnapshot; // 兑换券名称快照

    @Column(name = "coupon_image_snapshot", length = 500)
    private String couponImageSnapshot; // 兑换券图片快照

    @Column(name = "status", nullable = false)
    private Integer status = 1; // 1未使用 2已使用 3已过期

    @Column(name = "acquired_at")
    private LocalDateTime acquiredAt; // 获得时间

    @Column(name = "expire_at")
    private LocalDateTime expireAt; // 过期时间

    @Column(name = "used_at")
    private LocalDateTime usedAt; // 使用时间

    @Column(name = "order_id")
    private Long orderId; // 使用订单ID

    @PrePersist
    public void prePersist() {
        this.acquiredAt = LocalDateTime.now();
    }
}
