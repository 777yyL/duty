package com.duty.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 记录模板配置实体类
 *
 * @author duty-system
 * @since 2025-12-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("record_template")
public class RecordTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
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
     * 字段描述/说明
     */
    private String fieldDescription;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 输入类型：TEXT-文本框 TEXTAREA-多行文本 SELECT-下拉框
     */
    private String inputType;

    /**
     * 选项配置（JSON格式），用于SELECT类型
     */
    private String options;

    /**
     * 是否必填：1-必填 0-非必填
     */
    private Integer isRequired;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 状态：1-启用 0-禁用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 删除标识：0-未删除 1-已删除
     */
    @TableLogic
    private Integer deleted;
}
