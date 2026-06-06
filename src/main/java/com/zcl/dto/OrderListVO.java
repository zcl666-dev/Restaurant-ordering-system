package com.zcl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderListVO {

    private Long id;
    private String orderNo;
    private BigDecimal payAmount;
    private Integer orderStatus;
    private String diningType;
    private String tableNumber;
    private Integer itemCount;
    private LocalDateTime createdAt;
    private java.util.List<OrderItemVO> items;
}