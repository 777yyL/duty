package com.duty.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.duty.dto.RecordTemplateDTO;
import com.duty.entity.RecordTemplate;
import com.duty.mapper.RecordTemplateMapper;
import com.duty.service.RecordTemplateService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 记录模板配置Service实现类
 *
 * @author duty-system
 * @since 2025-12-26
 */
@Service
public class RecordTemplateServiceImpl extends ServiceImpl<RecordTemplateMapper, RecordTemplate> implements RecordTemplateService {

    @Override
    public List<RecordTemplateDTO> listAllEnabled() {
        return this.list(
                new LambdaQueryWrapper<RecordTemplate>()
                        .eq(RecordTemplate::getStatus, 1)
                        .orderByAsc(RecordTemplate::getSortOrder)
        ).stream().map(entity -> {
            RecordTemplateDTO dto = new RecordTemplateDTO();
            BeanUtil.copyProperties(entity, dto);
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public RecordTemplateDTO getDtoById(Long id) {
        RecordTemplate entity = this.getById(id);
        if (entity == null) {
            return null;
        }
        RecordTemplateDTO dto = new RecordTemplateDTO();
        BeanUtil.copyProperties(entity, dto);
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveDto(RecordTemplateDTO dto) {
        RecordTemplate entity = new RecordTemplate();
        BeanUtil.copyProperties(dto, entity);
        return this.save(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDto(RecordTemplateDTO dto) {
        RecordTemplate entity = new RecordTemplate();
        BeanUtil.copyProperties(dto, entity);
        return this.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteDto(Long id) {
        return this.removeById(id);
    }
}
