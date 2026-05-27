package com.zcl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 规格组 VO（包含规格值列表）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OptionGroupVO {

    /**
     * 规格组ID
     */
    private Long groupId;

    /**
     * 规格组名称
     */
    private String groupName;

    /**
     * 规格值列表
     */
    private List<OptionValueVO> options;
}
