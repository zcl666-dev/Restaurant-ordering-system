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
 * 用户兑换券实体类
 */
@Entity
@Table(name = "user_exchange_voucher")
@Data
@NoArgsConstructor
public class UserExchangeVoucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "points_mall_id", nullable = false)
    private PointsMall pointsMall;

    @Column(name = "voucher_code", nullable = false, length = 64)
    private String voucherCode; // 兑换券唯一码

    @Column(name = "required_points", nullable = false)
    private Integer requiredPoints; // 兑换时消耗的积分（快照）

    @Column(name = "status", nullable = false)
    private Integer status = 0; // 0未使用 1已使用 2已过期

    @Column(name = "expire_time", nullable = false)
    private LocalDateTime expireTime; // 过期时间

    @Column(name = "used_at")
    private LocalDateTime usedAt; // 使用时间

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
