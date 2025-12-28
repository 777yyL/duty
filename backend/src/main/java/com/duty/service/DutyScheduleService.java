package com.duty.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.duty.dto.DutyScheduleDTO;
import com.duty.dto.PersonDTO;
import com.duty.entity.DutySchedule;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 排班Service接口
 *
 * @author duty-system
 * @since 2025-12-26
 */
public interface DutyScheduleService extends IService<DutySchedule> {

    /**
     * 分页查询排班列表
     *
     * @param current 当前页
     * @param size 每页大小
     * @param personName 人员姓名（模糊查询）
     * @param deptId 部门ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 分页结果
     */
    Map<String, Object> pageList(Long current, Long size, String personName, String deptId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取月度排班表（日历视图）
     *
     * @param year 年份
     * @param month 月份
     * @return 月度排班数据
     */
    Map<String, Object> getMonthSchedule(Integer year, Integer month);

    /**
     * 获取指定日期的排班列表
     *
     * @param dutyDate 值班日期
     * @return 排班列表
     */
    List<DutyScheduleDTO> listByDate(LocalDate dutyDate);

    /**
     * 根据ID查询排班
     *
     * @param id 排班ID
     * @return 排班DTO
     */
    DutyScheduleDTO getDtoById(Long id);

    /**
     * 添加排班（支持批量）
     *
     * @param schedules 排班列表
     * @return 是否成功
     */
    boolean saveBatch(List<DutyScheduleDTO> schedules);

    /**
     * 更新排班
     *
     * @param dto 排班DTO
     * @return 是否成功
     */
    boolean updateDto(DutyScheduleDTO dto);

    /**
     * 删除排班
     *
     * @param id 排班ID
     * @return 是否成功
     */
    boolean deleteDto(Long id);

    /**
     * 取消排班
     *
     * @param id 排班ID
     * @return 是否成功
     */
    boolean cancelSchedule(Long id);

    /**
     * 从人员库搜索人员（预留接口）
     *
     * @param keyword 搜索关键字（姓名或部门）
     * @return 人员列表
     */
    List<PersonDTO> searchPersons(String keyword);

    /**
     * 获取部门树
     *
     * @return 部门树
     */
    List<Map<String, Object>> getDepartmentTree();

    /**
     * 根据部门ID获取人员列表
     *
     * @param deptId 部门ID（可选）
     * @param keyword 搜索关键字（可选）
     * @return 人员列表
     */
    List<PersonDTO> getPersonsByDept(String deptId, String keyword);
}
