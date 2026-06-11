package com.zcl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户端积分商城商品 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointsMallItemVO {
    private Long id;
    private Long productId;
    private String productName;
    private String productImage;
    private Integer pointsRequired;
    private Integer exchangeQuantity;   // 总量
    private Integer remainCount;        // 剩余，-1表示不限量
}
