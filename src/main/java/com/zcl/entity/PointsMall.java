package com.zcl.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 积分商城实体类
 */
@Entity
@Table(name = "points_mall")
@Data
@NoArgsConstructor
public class PointsMall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "points_required", nullable = false)
    private Integer pointsRequired; // 兑换所需积分

    @Column(name = "expire_days", nullable = false)
    private Integer expireDays = 7; // 兑换券有效天数

    @Column(name = "exchange_quantity", nullable = false)
    private Integer exchangeQuantity = 0; // 限制兑换数量，0表示不限量

    @Column(name = "redeemed_count", nullable = false)
    private Integer redeemedCount = 0; // 已兑换数量

    @Column(name = "status", nullable = false)
    private Integer status = 1; // 状态

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
