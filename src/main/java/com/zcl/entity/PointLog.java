package com.zcl.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 积分流水实体类
 */
@Entity
@Table(name = "point_log")
@Data
@NoArgsConstructor
public class PointLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Orders order; // 来源订单ID

    @Column(name = "user_coupon_id")
    private Long userCouponId; // 兑换券ID

    @Column(name = "type", nullable = false)
    private Integer type; // 1获得 2扣除

    @Column(name = "points_change", nullable = false)
    private Integer pointsChange; // 积分变动值

    @Column(name = "balance_after", nullable = false)
    private Integer balanceAfter; // 变动后积分

    @Column(name = "remark", length = 255)
    private String remark; // 备注

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
