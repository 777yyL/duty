package com.duty.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.duty.common.Result;
import com.duty.dto.DutyRecordDTO;
import com.duty.dto.SaveRecordDTO;
import com.duty.service.DutyRecordService;
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

/**
 * 值班记录Controller
 *
 * @author duty-system
 * @since 2025-12-26
 */
@Api(tags = "值班记录管理")
@Validated
@RestController
@RequestMapping("/record")
public class DutyRecordController {

    @Autowired
    private DutyRecordService dutyRecordService;

    /**
     * 分页查询值班记录列表
     */
    @ApiOperation("分页查询值班记录列表")
    @GetMapping("/page")
    public Result<Page<DutyRecordDTO>> page(
            @ApiParam("当前页") @RequestParam(defaultValue = "1") Long current,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Long size,
            @ApiParam("人员ID") @RequestParam(required = false) String personId,
            @ApiParam("人员姓名") @RequestParam(required = false) String personName,
            @ApiParam("开始日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @ApiParam("结束日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        Page<DutyRecordDTO> page = dutyRecordService.pageList(current, size, personId, personName, startDate, endDate);
        return Result.success(page);
    }

    /**
     * 根据ID查询值班记录详情
     */
    @ApiOperation("根据ID查询值班记录详情")
    @GetMapping("/{id}")
    public Result<DutyRecordDTO> getDetailById(
            @ApiParam("记录ID") @PathVariable @NotNull(message = "记录ID不能为空") Long id) {
        DutyRecordDTO dto = dutyRecordService.getDetailById(id);
        if (dto == null) {
            return Result.error("值班记录不存在");
        }
        return Result.success(dto);
    }

    /**
     * 根据排班ID查询值班记录
     */
    @ApiOperation("根据排班ID查询值班记录")
    @GetMapping("/schedule/{scheduleId}")
    public Result<DutyRecordDTO> getByScheduleId(
            @ApiParam("排班ID") @PathVariable @NotNull(message = "排班ID不能为空") Long scheduleId) {
        DutyRecordDTO dto = dutyRecordService.getByScheduleId(scheduleId);
        if (dto == null) {
            return Result.error("值班记录不存在");
        }
        return Result.success(dto);
    }

    /**
     * 保存值班记录
     */
    @ApiOperation("保存值班记录")
    @PostMapping
    public Result<Void> save(@Valid @RequestBody SaveRecordDTO dto) {
        try {
            boolean success = dutyRecordService.saveRecord(dto);
            return success ? Result.success("保存成功", null) : Result.error("保存失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新值班记录
     */
    @ApiOperation("更新值班记录")
    @PutMapping("/{id}")
    public Result<Void> update(
            @ApiParam("记录ID") @PathVariable @NotNull(message = "记录ID不能为空") Long id,
            @Valid @RequestBody SaveRecordDTO dto) {
        try {
            boolean success = dutyRecordService.updateRecord(id, dto);
            return success ? Result.success("更新成功", null) : Result.error("更新失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除值班记录
     */
    @ApiOperation("删除值班记录")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @ApiParam("记录ID") @PathVariable @NotNull(message = "记录ID不能为空") Long id) {
        boolean success = dutyRecordService.deleteRecord(id);
        return success ? Result.success("删除成功", null) : Result.error("删除失败");
    }
}
