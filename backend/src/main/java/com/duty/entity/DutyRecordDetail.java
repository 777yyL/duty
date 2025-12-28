package com.duty.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 值班记录明细实体类
 *
 * @author duty-system
 * @since 2025-12-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("duty_record_detail")
public class DutyRecordDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 值班记录ID，关联duty_record表
     */
    private Long recordId;

    /**
     * 模板ID，关联record_template表
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
     * 记录内容
     */
    private String recordContent;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

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
