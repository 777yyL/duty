package com.duty.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.duty.dto.*;
import com.duty.entity.*;
import com.duty.mapper.DutyRecordDetailMapper;
import com.duty.mapper.DutyRecordMapper;
import com.duty.service.DutyRecordService;
import com.duty.service.DutyScheduleService;
import com.duty.service.RecordTemplateService;
import com.duty.service.ShiftConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 值班记录Service实现类
 *
 * @author duty-system
 * @since 2025-12-26
 */
@Slf4j
@Service
public class DutyRecordServiceImpl extends ServiceImpl<DutyRecordMapper, DutyRecord> implements DutyRecordService {

    @Autowired
    private DutyRecordDetailMapper detailMapper;

    @Autowired
    private DutyScheduleService dutyScheduleService;

    @Autowired
    private ShiftConfigService shiftConfigService;

    @Autowired
    private RecordTemplateService recordTemplateService;

    @Override
    public Page<DutyRecordDTO> pageList(Long current, Long size, String personId, String personName, LocalDate startDate, LocalDate endDate) {
        // 查询值班记录主表
        Page<DutyRecord> page = this.page(
                new Page<>(current, size),
                new LambdaQueryWrapper<DutyRecord>()
                        .eq(StrUtil.isNotBlank(personId), DutyRecord::getPersonId, personId)
                        .like(StrUtil.isNotBlank(personName), DutyRecord::getPersonName, personName)
                        .ge(startDate != null, DutyRecord::getDutyDate, startDate)
                        .le(endDate != null, DutyRecord::getDutyDate, endDate)
                        .orderByDesc(DutyRecord::getDutyDate)
        );

        // 获取班次配置
        List<ShiftConfig> shiftConfigs = shiftConfigService.list();
        Map<Long, ShiftConfig> shiftMap = shiftConfigs.stream()
                .collect(Collectors.toMap(ShiftConfig::getId, s -> s));

        // 获取所有记录明细
        List<Long> recordIds = page.getRecords().stream()
                .map(DutyRecord::getId)
                .collect(Collectors.toList());

        Map<Long, List<DutyRecordDetail>> detailMap = new HashMap<>();
        if (CollUtil.isNotEmpty(recordIds)) {
            List<DutyRecordDetail> details = detailMapper.selectList(
                    new LambdaQueryWrapper<DutyRecordDetail>()
                            .in(DutyRecordDetail::getRecordId, recordIds)
            );
            detailMap = details.stream().collect(Collectors.groupingBy(DutyRecordDetail::getRecordId));
        }

        // 转换为DTO
        Map<Long, List<DutyRecordDetail>> finalDetailMap = detailMap;
        List<DutyRecordDTO> dtoList = page.getRecords().stream().map(entity -> {
            DutyRecordDTO dto = new DutyRecordDTO();
            BeanUtil.copyProperties(entity, dto);

            // 设置班次时间
            ShiftConfig shiftConfig = shiftMap.get(entity.getShiftId());
            if (shiftConfig != null) {
                dto.setShiftStartTime(shiftConfig.getStartTime());
                dto.setShiftEndTime(shiftConfig.getEndTime());
            }

            // 生成工作日志摘要
            List<DutyRecordDetail> details = finalDetailMap.get(entity.getId());
            if (CollUtil.isNotEmpty(details)) {
                String summary = details.stream()
                        .sorted(Comparator.comparing(d -> {
                            RecordTemplate template = recordTemplateService.getById(d.getTemplateId());
                            return template != null ? template.getSortOrder() : 0;
                        }))
                        .map(d -> "[" + d.getCategoryName() + "] " + (StrUtil.isNotBlank(d.getRecordContent()) ? d.getRecordContent() : ""))
                        .collect(Collectors.joining("；"));
                dto.setWorkLogSummary(summary);
            }

            return dto;
        }).collect(Collectors.toList());

        Page<DutyRecordDTO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(dtoList);

        return result;
    }

    @Override
    public DutyRecordDTO getDetailById(Long id) {
        DutyRecord record = this.getById(id);
        if (record == null) {
            return null;
        }

        DutyRecordDTO dto = new DutyRecordDTO();
        BeanUtil.copyProperties(record, dto);

        // 获取班次时间
        ShiftConfig shiftConfig = shiftConfigService.getById(record.getShiftId());
        if (shiftConfig != null) {
            dto.setShiftStartTime(shiftConfig.getStartTime());
            dto.setShiftEndTime(shiftConfig.getEndTime());
        }

        // 获取记录明细
        List<DutyRecordDetail> details = detailMapper.selectList(
                new LambdaQueryWrapper<DutyRecordDetail>()
                        .eq(DutyRecordDetail::getRecordId, id)
        );

        // 获取所有模板配置
        List<RecordTemplate> templates = recordTemplateService.list();
        Map<Long, RecordTemplate> templateMap = templates.stream()
                .collect(Collectors.toMap(RecordTemplate::getId, t -> t));

        // 转换明细DTO
        List<DutyRecordDetailDTO> detailDTOList = details.stream().map(detail -> {
            DutyRecordDetailDTO detailDTO = new DutyRecordDetailDTO();
            BeanUtil.copyProperties(detail, detailDTO);

            RecordTemplate template = templateMap.get(detail.getTemplateId());
            if (template != null) {
                detailDTO.setFieldDescription(template.getFieldDescription());
                detailDTO.setDefaultValue(template.getDefaultValue());
                detailDTO.setInputType(template.getInputType());
                detailDTO.setIsRequired(template.getIsRequired());
            }

            return detailDTO;
        }).collect(Collectors.toList());

        dto.setDetailList(detailDTOList);

        return dto;
    }

    @Override
    public DutyRecordDTO getByScheduleId(Long scheduleId) {
        DutyRecord record = this.getOne(
                new LambdaQueryWrapper<DutyRecord>()
                        .eq(DutyRecord::getScheduleId, scheduleId)
        );

        if (record == null) {
            return null;
        }

        return getDetailById(record.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveRecord(SaveRecordDTO dto) {
        // 获取排班信息
        DutySchedule schedule = dutyScheduleService.getById(dto.getScheduleId());
        if (schedule == null) {
            throw new RuntimeException("排班记录不存在");
        }

        // 创建值班记录主表
        DutyRecord record = new DutyRecord();
        record.setScheduleId(dto.getScheduleId());
        record.setDutyDate(schedule.getDutyDate());
        record.setShiftId(schedule.getShiftId());
        record.setShiftName(schedule.getShiftName());
        record.setPersonId(schedule.getPersonId());
        record.setPersonName(schedule.getPersonName());
        record.setRecordTime(java.time.LocalDateTime.now());
        record.setStatus(1);

        boolean saved = this.save(record);
        if (!saved) {
            return false;
        }

        // 保存记录明细
        List<DutyRecordDetail> details = dto.getDetails().stream().map(item -> {
            DutyRecordDetail detail = new DutyRecordDetail();
            detail.setRecordId(record.getId());
            detail.setTemplateId(item.getTemplateId());

            // 获取模板信息
            RecordTemplate template = recordTemplateService.getById(item.getTemplateId());
            if (template != null) {
                detail.setCategoryName(template.getCategoryName());
                detail.setCategoryCode(template.getCategoryCode());
            }

            detail.setRecordContent(item.getRecordContent());
            return detail;
        }).collect(Collectors.toList());

        // 批量保存明细
        for (DutyRecordDetail detail : details) {
            detailMapper.insert(detail);
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRecord(Long id, SaveRecordDTO dto) {
        // 检查记录是否存在
        DutyRecord existRecord = this.getById(id);
        if (existRecord == null) {
            throw new RuntimeException("值班记录不存在");
        }

        // 删除原有明细
        detailMapper.delete(
                new LambdaQueryWrapper<DutyRecordDetail>()
                        .eq(DutyRecordDetail::getRecordId, id)
        );

        // 保存新的明细
        List<DutyRecordDetail> details = dto.getDetails().stream().map(item -> {
            DutyRecordDetail detail = new DutyRecordDetail();
            detail.setRecordId(id);
            detail.setTemplateId(item.getTemplateId());

            // 获取模板信息
            RecordTemplate template = recordTemplateService.getById(item.getTemplateId());
            if (template != null) {
                detail.setCategoryName(template.getCategoryName());
                detail.setCategoryCode(template.getCategoryCode());
            }

            detail.setRecordContent(item.getRecordContent());
            return detail;
        }).collect(Collectors.toList());

        // 批量保存明细
        for (DutyRecordDetail detail : details) {
            detailMapper.insert(detail);
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRecord(Long id) {
        // 删除明细
        detailMapper.delete(
                new LambdaQueryWrapper<DutyRecordDetail>()
                        .eq(DutyRecordDetail::getRecordId, id)
        );

        // 删除主记录
        return this.removeById(id);
    }
}
