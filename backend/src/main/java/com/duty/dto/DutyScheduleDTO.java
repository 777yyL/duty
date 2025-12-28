package com.duty.dto;

import lombok.Data;
import lombok.NonNull;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 排班DTO
 *
 * @author duty-system
 * @since 2025-12-26
 */
@Data
public class DutyScheduleDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 排班ID
     */
    private Long id;

    /**
     * 值班日期
     */
    @NotNull(message = "dutyDate is not empty")
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
     * 人员性别
     */
    private String personGender;

    /**
     * 部门ID
     */
    private String deptId;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 是否已填写记录
     */
    private Boolean hasRecord;
}
