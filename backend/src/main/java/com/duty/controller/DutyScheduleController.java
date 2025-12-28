package com.duty.controller;

import com.duty.common.Result;
import com.duty.dto.DutyScheduleDTO;
import com.duty.dto.PersonDTO;
import com.duty.service.DutyScheduleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 排班管理Controller
 *
 * @author duty-system
 * @since 2025-12-26
 */
@Api(tags = "排班管理")
@Validated
@RestController
@RequestMapping("/schedule")
public class DutyScheduleController {

    @Autowired
    private DutyScheduleService dutyScheduleService;

    /**
     * 分页查询排班列表
     */
    @ApiOperation("分页查询排班列表")
    @GetMapping("/page")
    public Result<Map<String, Object>> page(
            @ApiParam("当前页") @RequestParam(defaultValue = "1") Long current,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Long size,
            @ApiParam("人员姓名") @RequestParam(required = false) String personName,
            @ApiParam("部门ID") @RequestParam(required = false) String deptId,
            @ApiParam("开始日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @ApiParam("结束日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        Map<String, Object> result = dutyScheduleService.pageList(current, size, personName, deptId, startDate, endDate);
        return Result.success(result);
    }

    /**
     * 获取月度排班表（日历视图）
     */
    @ApiOperation("获取月度排班表")
    @GetMapping("/month/{year}/{month}")
    public Result<Map<String, Object>> getMonthSchedule(
            @ApiParam("年份") @PathVariable Integer year,
            @ApiParam("月份") @PathVariable Integer month) {
        Map<String, Object> result = dutyScheduleService.getMonthSchedule(year, month);
        return Result.success(result);
    }

    /**
     * 获取指定日期的排班列表
     */
    @ApiOperation("获取指定日期的排班列表")
    @GetMapping("/date/{date}")
    public Result<List<DutyScheduleDTO>> listByDate(
            @ApiParam("值班日期") @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        List<DutyScheduleDTO> list = dutyScheduleService.listByDate(date);
        return Result.success(list);
    }

    /**
     * 根据ID查询排班
     */
    @ApiOperation("根据ID查询排班")
    @GetMapping("/{id}")
    public Result<DutyScheduleDTO> getById(
            @ApiParam("排班ID") @PathVariable @NotNull(message = "排班ID不能为空") Long id) {
        DutyScheduleDTO dto = dutyScheduleService.getDtoById(id);
        if (dto == null) {
            return Result.error("排班不存在");
        }
        return Result.success(dto);
    }

    /**
     * 添加排班（支持批量）
     */
    @ApiOperation("添加排班")
    @PostMapping("/batch")
    public Result<Void> saveBatch(@Valid @RequestBody List<DutyScheduleDTO> schedules) {
        boolean success = dutyScheduleService.saveBatch(schedules);
        return success ? Result.success("添加成功", null) : Result.error("添加失败");
    }

    /**
     * 更新排班
     */
    @ApiOperation("更新排班")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody DutyScheduleDTO dto) {
        if (dto.getId() == null) {
            return Result.error("排班ID不能为空");
        }
        boolean success = dutyScheduleService.updateDto(dto);
        return success ? Result.success("更新成功", null) : Result.error("更新失败");
    }

    /**
     * 删除排班
     */
    @ApiOperation("删除排班")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @ApiParam("排班ID") @PathVariable @NotNull(message = "排班ID不能为空") Long id) {
        boolean success = dutyScheduleService.deleteDto(id);
        return success ? Result.success("删除成功", null) : Result.error("删除失败");
    }

    /**
     * 取消排班
     */
    @ApiOperation("取消排班")
    @PutMapping("/cancel/{id}")
    public Result<Void> cancel(
            @ApiParam("排班ID") @PathVariable @NotNull(message = "排班ID不能为空") Long id) {
        boolean success = dutyScheduleService.cancelSchedule(id);
        return success ? Result.success("取消成功", null) : Result.error("取消失败");
    }

    /**
     * 搜索人员（从人员库）
     */
    @ApiOperation("搜索人员")
    @GetMapping("/person/search")
    public Result<List<PersonDTO>> searchPersons(
            @ApiParam("搜索关键字") @RequestParam(required = false) String keyword) {
        List<PersonDTO> list = dutyScheduleService.searchPersons(keyword);
        return Result.success(list);
    }

    /**
     * 获取部门树
     */
    @ApiOperation("获取部门树")
    @GetMapping("/departments")
    public Result<List<Map<String, Object>>> getDepartmentTree() {
        List<Map<String, Object>> tree = dutyScheduleService.getDepartmentTree();
        return Result.success(tree);
    }

    /**
     * 根据部门ID获取人员列表
     */
    @ApiOperation("根据部门获取人员")
    @GetMapping("/person/department")
    public Result<List<PersonDTO>> getPersonsByDept(
            @ApiParam("部门ID") @RequestParam(required = false) String deptId,
            @ApiParam("搜索关键字") @RequestParam(required = false) String keyword) {
        List<PersonDTO> list = dutyScheduleService.getPersonsByDept(deptId, keyword);
        return Result.success(list);
    }
}
