package com.duty.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.duty.dto.RecordTemplateDTO;
import com.duty.entity.RecordTemplate;

import java.util.List;

/**
 * 记录模板配置Service接口
 *
 * @author duty-system
 * @since 2025-12-26
 */
public interface RecordTemplateService extends IService<RecordTemplate> {

    /**
     * 获取所有启用的模板配置
     *
     * @return 模板配置列表
     */
    List<RecordTemplateDTO> listAllEnabled();

    /**
     * 根据ID查询模板配置
     *
     * @param id 模板ID
     * @return 模板配置DTO
     */
    RecordTemplateDTO getDtoById(Long id);

    /**
     * 新增模板配置
     *
     * @param dto 模板配置DTO
     * @return 是否成功
     */
    boolean saveDto(RecordTemplateDTO dto);

    /**
     * 更新模板配置
     *
     * @param dto 模板配置DTO
     * @return 是否成功
     */
    boolean updateDto(RecordTemplateDTO dto);

    /**
     * 删除模板配置
     *
     * @param id 模板ID
     * @return 是否成功
     */
    boolean deleteDto(Long id);
}
