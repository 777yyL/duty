package com.duty.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 人员DTO
 * 来自人员库系统
 *
 * @author duty-system
 * @since 2025-12-26
 */
@Data
public class PersonDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 人员ID
     */
    private String personId;

    /**
     * 人员姓名
     */
    private String personName;

    /**
     * 性别
     */
    private String gender;

    /**
     * 部门ID
     */
    private String deptId;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 职位
     */
    private String position;
}
