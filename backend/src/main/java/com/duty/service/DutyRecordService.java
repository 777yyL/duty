package com.duty.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.duty.dto.DutyRecordDTO;
import com.duty.dto.SaveRecordDTO;
import com.duty.entity.DutyRecord;

import java.time.LocalDate;
import java.util.Map;

/**
 * 值班记录Service接口
 *
 * @author duty-system
 * @since 2025-12-26
 */
public interface DutyRecordService extends IService<DutyRecord> {

    /**
     * 分页查询值班记录列表
     *
     * @param current 当前页
     * @param size 每页大小
     * @param personId 人员ID
     * @param personName 人员姓名
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 分页结果
     */
    Page<DutyRecordDTO> pageList(Long current, Long size, String personId, String personName, LocalDate startDate, LocalDate endDate);

    /**
     * 根据ID查询值班记录详情
     *
     * @param id 记录ID
     * @return 值班记录DTO
     */
    DutyRecordDTO getDetailById(Long id);

    /**
     * 根据排班ID查询值班记录
     *
     * @param scheduleId 排班ID
     * @return 值班记录DTO
     */
    DutyRecordDTO getByScheduleId(Long scheduleId);

    /**
     * 保存值班记录
     *
     * @param dto 保存记录DTO
     * @return 是否成功
     */
    boolean saveRecord(SaveRecordDTO dto);

    /**
     * 更新值班记录
     *
     * @param id 记录ID
     * @param dto 保存记录DTO
     * @return 是否成功
     */
    boolean updateRecord(Long id, SaveRecordDTO dto);

    /**
     * 删除值班记录
     *
     * @param id 记录ID
     * @return 是否成功
     */
    boolean deleteRecord(Long id);
}
