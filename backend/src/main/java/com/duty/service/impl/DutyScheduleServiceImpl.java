package com.duty.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.duty.dto.DutyScheduleDTO;
import com.duty.dto.PersonDTO;
import com.duty.dto.ShiftConfigDTO;
import com.duty.entity.DutySchedule;
import com.duty.entity.ShiftConfig;
import com.duty.mapper.DutyScheduleMapper;
import com.duty.mapper.DutyRecordMapper;
import com.duty.service.DutyScheduleService;
import com.duty.service.ShiftConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 排班Service实现类
 *
 * @author duty-system
 * @since 2025-12-26
 */
@Slf4j
@Service
public class DutyScheduleServiceImpl extends ServiceImpl<DutyScheduleMapper, DutySchedule> implements DutyScheduleService {

    @Autowired(required = false)
    private DutyRecordMapper dutyRecordMapper;

    @Autowired
    private ShiftConfigService shiftConfigService;

    @Override
    public Map<String, Object> pageList(Long current, Long size, String personName, String deptId, LocalDate startDate, LocalDate endDate) {
        Page<DutySchedule> page = this.page(
                new Page<>(current, size),
                new LambdaQueryWrapper<DutySchedule>()
                        .like(StrUtil.isNotBlank(personName), DutySchedule::getPersonName, personName)
                        .eq(StrUtil.isNotBlank(deptId), DutySchedule::getDeptId, deptId)
                        .ge(startDate != null, DutySchedule::getDutyDate, startDate)
                        .le(endDate != null, DutySchedule::getDutyDate, endDate)
                        .orderByDesc(DutySchedule::getDutyDate)
        );

        List<DutyScheduleDTO> records = convertToDtoList(page.getRecords());

        Map<String, Object> result = new HashMap<>();
        result.put("current", page.getCurrent());
        result.put("size", page.getSize());
        result.put("total", page.getTotal());
        result.put("pages", page.getPages());
        result.put("records", records);

        return result;
    }

    @Override
    public Map<String, Object> getMonthSchedule(Integer year, Integer month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<DutySchedule> schedules = baseMapper.selectByDateRange(startDate, endDate);
        List<DutyScheduleDTO> dtoList = convertToDtoList(schedules);

        // 按日期分组
        Map<String, List<DutyScheduleDTO>> dateScheduleMap = dtoList.stream()
                .collect(Collectors.groupingBy(d -> d.getDutyDate().toString()));

        // 获取所有班次
        List<ShiftConfigDTO> shiftList = shiftConfigService.listAllEnabled();

        Map<String, Object> result = new HashMap<>();
        result.put("year", year);
        result.put("month", month);
        result.put("schedules", dateScheduleMap);
        result.put("shifts", shiftList);

        return result;
    }

    @Override
    public List<DutyScheduleDTO> listByDate(LocalDate dutyDate) {
        List<DutySchedule> schedules = baseMapper.selectByDate(dutyDate);
        return convertToDtoList(schedules);
    }

    @Override
    public DutyScheduleDTO getDtoById(Long id) {
        DutySchedule entity = this.getById(id);
        if (entity == null) {
            return null;
        }
        List<DutyScheduleDTO> dtoList = convertToDtoList(List.of(entity));
        return dtoList.isEmpty() ? null : dtoList.get(0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveBatch(List<DutyScheduleDTO> schedules) {
        if (CollUtil.isEmpty(schedules)) {
            return true;
        }

        // 获取第一排班的日期
        LocalDate dutyDate = schedules.get(0).getDutyDate();

        // 先删除该日期的所有排班（使用逻辑删除）
        LambdaQueryWrapper<DutySchedule> eq = Wrappers.lambdaQuery(DutySchedule.class)
                .eq(DutySchedule::getDutyDate, dutyDate);
        remove(eq);

        List<DutySchedule> entities = schedules.stream().map(dto -> {
            DutySchedule entity = new DutySchedule();
            BeanUtil.copyProperties(dto, entity);
            // 清空ID，确保是新增而不是更新
            entity.setId(null);
            // 从班次配置中获取班次名称
            ShiftConfig shiftConfig = shiftConfigService.getById(dto.getShiftId());
            if (shiftConfig != null) {
                entity.setShiftName(shiftConfig.getShiftName());
            }
            return entity;
        }).collect(Collectors.toList());

        return saveBatch(entities);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDto(DutyScheduleDTO dto) {
        DutySchedule entity = new DutySchedule();
        BeanUtil.copyProperties(dto, entity);
        return updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteDto(Long id) {
        return removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelSchedule(Long id) {
        DutySchedule schedule = getById(id);
        if (schedule == null) {
            return false;
        }
        schedule.setStatus(0);
        return updateById(schedule);
    }

    @Override
    public List<PersonDTO> searchPersons(String keyword) {
        // TODO: 预留人员库接口调用逻辑
        // 目前返回模拟数据
        log.info("搜索人员，关键字: {}", keyword);

        List<PersonDTO> mockPersons = new ArrayList<>();

        // 模拟数据 - 监控科
        if (StrUtil.isBlank(keyword) || "张".contains(keyword) || "一".contains(keyword)) {
            PersonDTO person1 = new PersonDTO();
            person1.setPersonId("P001");
            person1.setPersonName("张三");
            person1.setGender("男");
            person1.setDeptId("D001");
            person1.setDeptName("监控科");
            person1.setPhone("13800138001");
            person1.setPosition("科员");
            mockPersons.add(person1);
        }

        if (StrUtil.isBlank(keyword) || "李".contains(keyword) || "四".contains(keyword)) {
            PersonDTO person2 = new PersonDTO();
            person2.setPersonId("P002");
            person2.setPersonName("李四");
            person2.setGender("女");
            person2.setDeptId("D001");
            person2.setDeptName("监控科");
            person2.setPhone("13800138002");
            person2.setPosition("副科长");
            mockPersons.add(person2);
        }

        if (StrUtil.isBlank(keyword) || "赵".contains(keyword) || "六".contains(keyword)) {
            PersonDTO person4 = new PersonDTO();
            person4.setPersonId("P004");
            person4.setPersonName("赵六");
            person4.setGender("男");
            person4.setDeptId("D001");
            person4.setDeptName("监控科");
            person4.setPhone("13800138004");
            person4.setPosition("科员");
            mockPersons.add(person4);
        }

        // 模拟数据 - 安保科
        if (StrUtil.isBlank(keyword) || "王".contains(keyword) || "五".contains(keyword)) {
            PersonDTO person3 = new PersonDTO();
            person3.setPersonId("P003");
            person3.setPersonName("王五");
            person3.setGender("男");
            person3.setDeptId("D002");
            person3.setDeptName("安保科");
            person3.setPhone("13800138003");
            person3.setPosition("科长");
            mockPersons.add(person3);
        }

        if (StrUtil.isBlank(keyword) || "孙".contains(keyword) || "七".contains(keyword)) {
            PersonDTO person5 = new PersonDTO();
            person5.setPersonId("P005");
            person5.setPersonName("孙七");
            person5.setGender("女");
            person5.setDeptId("D002");
            person5.setDeptName("安保科");
            person5.setPhone("13800138005");
            person5.setPosition("科员");
            mockPersons.add(person5);
        }

        // 模拟数据 - 综合科
        if (StrUtil.isBlank(keyword) || "周".contains(keyword) || "八".contains(keyword)) {
            PersonDTO person6 = new PersonDTO();
            person6.setPersonId("P006");
            person6.setPersonName("周八");
            person6.setGender("男");
            person6.setDeptId("D003");
            person6.setDeptName("综合科");
            person6.setPhone("13800138006");
            person6.setPosition("科长");
            mockPersons.add(person6);
        }

        if (StrUtil.isBlank(keyword) || "吴".contains(keyword) || "九".contains(keyword)) {
            PersonDTO person7 = new PersonDTO();
            person7.setPersonId("P007");
            person7.setPersonName("吴九");
            person7.setGender("女");
            person7.setDeptId("D003");
            person7.setDeptName("综合科");
            person7.setPhone("13800138007");
            person7.setPosition("科员");
            mockPersons.add(person7);
        }

        return mockPersons;
    }

    @Override
    public List<Map<String, Object>> getDepartmentTree() {
        // TODO: 预留部门组织接口调用逻辑
        // 目前返回模拟数据
        log.info("获取部门树");

        List<Map<String, Object>> tree = new ArrayList<>();

        // 一级部门
        Map<String, Object> dept1 = new HashMap<>();
        dept1.put("deptId", "ROOT");
        dept1.put("deptName", "留置管理中心");
        dept1.put("parentId", "");

        List<Map<String, Object>> children = new ArrayList<>();

        // 二级部门
        Map<String, Object> subDept1 = new HashMap<>();
        subDept1.put("deptId", "D001");
        subDept1.put("deptName", "监控科");
        subDept1.put("parentId", "ROOT");
        subDept1.put("personCount", 3);
        children.add(subDept1);

        Map<String, Object> subDept2 = new HashMap<>();
        subDept2.put("deptId", "D002");
        subDept2.put("deptName", "安保科");
        subDept2.put("parentId", "ROOT");
        subDept2.put("personCount", 2);
        children.add(subDept2);

        Map<String, Object> subDept3 = new HashMap<>();
        subDept3.put("deptId", "D003");
        subDept3.put("deptName", "综合科");
        subDept3.put("parentId", "ROOT");
        subDept3.put("personCount", 2);
        children.add(subDept3);

        dept1.put("children", children);
        tree.add(dept1);

        return tree;
    }

    @Override
    public List<PersonDTO> getPersonsByDept(String deptId, String keyword) {
        // TODO: 预留人员库接口调用逻辑
        // 目前返回模拟数据
        log.info("获取部门人员，部门ID: {}, 关键字: {}", deptId, keyword);

        List<PersonDTO> allPersons = searchPersons(null);

        // 根据部门ID过滤
        List<PersonDTO> filtered = allPersons.stream()
                .filter(p -> StrUtil.isBlank(deptId) || p.getDeptId().equals(deptId))
                .filter(p -> StrUtil.isBlank(keyword) ||
                        p.getPersonName().contains(keyword) ||
                        p.getDeptName().contains(keyword))
                .collect(Collectors.toList());

        return filtered;
    }

    /**
     * 转换为DTO列表
     */
    private List<DutyScheduleDTO> convertToDtoList(List<DutySchedule> schedules) {
        if (CollUtil.isEmpty(schedules)) {
            return new ArrayList<>();
        }

        // 获取所有班次配置
        List<ShiftConfig> shiftConfigs = shiftConfigService.list();
        Map<Long, ShiftConfig> shiftMap = shiftConfigs.stream()
                .collect(Collectors.toMap(ShiftConfig::getId, s -> s));

        return schedules.stream().map(entity -> {
            DutyScheduleDTO dto = new DutyScheduleDTO();
            BeanUtil.copyProperties(entity, dto);

            // 设置班次时间
            ShiftConfig shiftConfig = shiftMap.get(entity.getShiftId());
            if (shiftConfig != null) {
                dto.setShiftStartTime(shiftConfig.getStartTime());
                dto.setShiftEndTime(shiftConfig.getEndTime());
            }

            // TODO: 查询是否已填写记录
            dto.setHasRecord(false);

            return dto;
        }).collect(Collectors.toList());
    }
}
