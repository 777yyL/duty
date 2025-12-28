package com.duty.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 班次配置DTO
 *
 * @author duty-system
 * @since 2025-12-26
 */
@Data
public class ShiftConfigDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 班次ID
     */
    private Long id;

    /**
     * 班次名称
     */
    private String shiftName;

    /**
     * 班次代码
     */
    private String shiftCode;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}
