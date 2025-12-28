package com.duty.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 记录模板DTO
 *
 * @author duty-system
 * @since 2025-12-26
 */
@Data
public class RecordTemplateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 模板ID
     */
    private Long id;

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
     * 默认值
     */
    private String defaultValue;

    /**
     * 输入类型
     */
    private String inputType;

    /**
     * 选项配置
     */
    private String options;

    /**
     * 是否必填
     */
    private Integer isRequired;

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
