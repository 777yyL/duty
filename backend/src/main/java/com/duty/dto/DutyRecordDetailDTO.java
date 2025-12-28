package com.duty.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 值班记录明细DTO
 *
 * @author duty-system
 * @since 2025-12-26
 */
@Data
public class DutyRecordDetailDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 明细ID
     */
    private Long id;

    /**
     * 记录ID
     */
    private Long recordId;

    /**
     * 模板ID
     */
    private Long templateId;

    /**
     * 类别名称
     */
    private String categoryName;

    /**
     * 类别代码
     */
    private String categoryCode;

    /**
     * 字段描述
     */
    private String fieldDescription;

    /**
     * 记录内容
     */
    private String recordContent;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 输入类型
     */
    private String inputType;

    /**
     * 是否必填
     */
    private Integer isRequired;
}
