package com.zcl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 规格值 VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OptionValueVO {

    /**
     * 规格值ID
     */
    private Long id;

    /**
     * 规格值名称
     */
    private String valueName;

    /**
     * 是否默认
     */
    private Boolean isDefault;
}
