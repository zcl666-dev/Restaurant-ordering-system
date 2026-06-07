package com.zcl.dto;

import lombok.Data;

@Data
public class BatchGenerateResult {
    private int successCount;
    private int failCount;
    private long timeCost;
}
