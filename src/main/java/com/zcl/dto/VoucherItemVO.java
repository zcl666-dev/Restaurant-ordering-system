package com.zcl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户兑换券列表项 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherItemVO {

    private Long id;
    private Long productId;
    private String productName;
    private String productImage;
    private Integer requiredPoints;
    private Integer status; // 0未使用 1已使用 2已过期
    private LocalDateTime expireTime;
    private LocalDateTime usedAt;
    private LocalDateTime createdAt;
}
