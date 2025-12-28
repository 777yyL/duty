package com.duty.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.duty.dto.ShiftConfigDTO;
import com.duty.entity.ShiftConfig;

import java.util.List;

/**
 * 班次配置Service接口
 *
 * @author duty-system
 * @since 2025-12-26
 */
public interface ShiftConfigService extends IService<ShiftConfig> {

    /**
     * 分页查询班次配置列表
     *
     * @param current 当前页
     * @param size 每页大小
     * @param shiftName 班次名称（模糊查询）
     * @param status 状态
     * @return 分页结果
     */
    Page<ShiftConfigDTO> pageList(Long current, Long size, String shiftName, Integer status);

    /**
     * 获取所有启用的班次配置
     *
     * @return 班次配置列表
     */
    List<ShiftConfigDTO> listAllEnabled();

    /**
     * 根据ID查询班次配置
     *
     * @param id 班次ID
     * @return 班次配置DTO
     */
    ShiftConfigDTO getDtoById(Long id);

    /**
     * 新增班次配置
     *
     * @param dto 班次配置DTO
     * @return 是否成功
     */
    boolean saveDto(ShiftConfigDTO dto);

    /**
     * 更新班次配置
     *
     * @param dto 班次配置DTO
     * @return 是否成功
     */
    boolean updateDto(ShiftConfigDTO dto);

    /**
     * 删除班次配置
     *
     * @param id 班次ID
     * @return 是否成功
     */
    boolean deleteDto(Long id);
}
