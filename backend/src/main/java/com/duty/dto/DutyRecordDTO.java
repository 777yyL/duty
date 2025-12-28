package com.duty.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 值班记录DTO
 *
 * @author duty-system
 * @since 2025-12-26
 */
@Data
public class DutyRecordDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 记录ID
     */
    private Long id;

    /**
     * 排班ID
     */
    private Long scheduleId;

    /**
     * 值班日期
     */
    private LocalDate dutyDate;

    /**
     * 班次ID
     */
    private Long shiftId;

    /**
     * 班次名称
     */
    private String shiftName;

    /**
     * 班次开始时间
     */
    private String shiftStartTime;

    /**
     * 班次结束时间
     */
    private String shiftEndTime;

    /**
     * 人员ID
     */
    private String personId;

    /**
     * 人员姓名
     */
    private String personName;

    /**
     * 记录填写时间
     */
    private LocalDateTime recordTime;

    /**
     * 工作日志摘要
     */
    private String workLogSummary;

    /**
     * 记录明细列表
     */
    private List<DutyRecordDetailDTO> detailList;

    /**
     * 状态
     */
    private Integer status;
}
