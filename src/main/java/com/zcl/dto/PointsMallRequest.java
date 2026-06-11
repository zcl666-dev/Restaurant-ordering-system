package com.zcl.dto;

import lombok.Data;

/**
 * 积分商城创建/编辑请求
 */
@Data
public class PointsMallRequest {
    private Long id;
    private Long productId;
    private Integer pointsRequired;
    private Integer exchangeQuantity;
    private Integer expireDays;
    private Integer status;
}
