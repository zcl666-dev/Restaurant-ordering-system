package com.zcl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailVO {

    private Long id;
    private String orderNo;
    private Integer orderStatus;
    private Integer paymentStatus;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal payAmount;
    private String diningType;
    private String tableNumber;
    private String remark;
    private LocalDateTime createdAt;
    private List<OrderItemVO> items;
}
