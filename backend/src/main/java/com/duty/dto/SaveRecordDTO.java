package com.duty.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 保存值班记录DTO
 *
 * @author duty-system
 * @since 2025-12-26
 */
@Data
public class SaveRecordDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 排班ID
     */
    @NotNull(message = "排班ID不能为空")
    private Long scheduleId;

    /**
     * 记录明细列表
     */
    @NotEmpty(message = "记录明细不能为空")
    private List<RecordDetailItem> details;

    /**
     * 记录明细项
     */
    @Data
    public static class RecordDetailItem implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 模板ID
         */
        @NotNull(message = "模板ID不能为空")
        private Long templateId;

        /**
         * 记录内容
         */
        private String recordContent;
    }
}
