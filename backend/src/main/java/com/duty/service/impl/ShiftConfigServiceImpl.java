package com.duty.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.duty.dto.ShiftConfigDTO;
import com.duty.entity.ShiftConfig;
import com.duty.mapper.ShiftConfigMapper;
import com.duty.service.ShiftConfigService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 班次配置Service实现类
 *
 * @author duty-system
 * @since 2025-12-26
 */
@Service
public class ShiftConfigServiceImpl extends ServiceImpl<ShiftConfigMapper, ShiftConfig> implements ShiftConfigService {

    @Override
    public Page<ShiftConfigDTO> pageList(Long current, Long size, String shiftName, Integer status) {
        Page<ShiftConfig> page = this.page(
                new Page<>(current, size),
                new LambdaQueryWrapper<ShiftConfig>()
                        .like(StrUtil.isNotBlank(shiftName), ShiftConfig::getShiftName, shiftName)
                        .eq(status != null, ShiftConfig::getStatus, status)
                        .orderByAsc(ShiftConfig::getSortOrder)
                        .orderByDesc(ShiftConfig::getCreateTime)
        );

        Page<ShiftConfigDTO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        List<ShiftConfigDTO> records = page.getRecords().stream()
                .map(entity -> {
                    ShiftConfigDTO dto = new ShiftConfigDTO();
                    BeanUtil.copyProperties(entity, dto);
                    return dto;
                })
                .collect(Collectors.toList());
        result.setRecords(records);

        return result;
    }

    @Override
    public List<ShiftConfigDTO> listAllEnabled() {
        return this.list(
                new LambdaQueryWrapper<ShiftConfig>()
                        .eq(ShiftConfig::getStatus, 1)
                        .orderByAsc(ShiftConfig::getSortOrder)
        ).stream().map(entity -> {
            ShiftConfigDTO dto = new ShiftConfigDTO();
            BeanUtil.copyProperties(entity, dto);
            return dto;
        })     .collect(Collectors.toList());
    }

    @Override
    public ShiftConfigDTO getDtoById(Long id) {
        ShiftConfig entity = this.getById(id);
        if (entity == null) {
            return null;
        }
        ShiftConfigDTO dto = new ShiftConfigDTO();
        BeanUtil.copyProperties(entity, dto);
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveDto(ShiftConfigDTO dto) {
        ShiftConfig entity = new ShiftConfig();
        BeanUtil.copyProperties(dto, entity);
        return this.save(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDto(ShiftConfigDTO dto) {
        ShiftConfig entity = new ShiftConfig();
        BeanUtil.copyProperties(dto, entity);
        return this.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteDto(Long id) {
        return this.removeById(id);
    }
}
