package com.zcl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 管理端积分商城 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminPointsMallVO {
    private Long id;
    private Long productId;
    private String productName;
    private String productImage;
    private Integer pointsRequired;
    private Integer exchangeQuantity;
    private Integer redeemedCount;
    private Integer remainCount;    // 剩余可兑换数量
    private Integer expireDays;
    private Integer status;         // 0下架 1上架
    private LocalDateTime createdAt;
}
