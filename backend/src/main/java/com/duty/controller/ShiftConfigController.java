package com.duty.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.duty.common.Result;
import com.duty.dto.ShiftConfigDTO;
import com.duty.service.ShiftConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 班次配置Controller
 *
 * @author duty-system
 * @since 2025-12-26
 */
@Api(tags = "班次配置管理")
@Validated
@RestController
@RequestMapping("/shift")
public class ShiftConfigController {

    @Autowired
    private ShiftConfigService shiftConfigService;

    /**
     * 分页查询班次配置列表
     */
    @ApiOperation("分页查询班次配置列表")
    @GetMapping("/page")
    public Result<Page<ShiftConfigDTO>> page(
            @ApiParam("当前页") @RequestParam(defaultValue = "1") Long current,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Long size,
            @ApiParam("班次名称") @RequestParam(required = false) String shiftName,
            @ApiParam("状态") @RequestParam(required = false) Integer status) {
        Page<ShiftConfigDTO> page = shiftConfigService.pageList(current, size, shiftName, status);
        return Result.success(page);
    }

    /**
     * 获取所有启用的班次配置
     */
    @ApiOperation("获取所有启用的班次配置")
    @GetMapping("/list/enabled")
    public Result<List<ShiftConfigDTO>> listEnabled() {
        List<ShiftConfigDTO> list = shiftConfigService.listAllEnabled();
        return Result.success(list);
    }

    /**
     * 根据ID查询班次配置
     */
    @ApiOperation("根据ID查询班次配置")
    @GetMapping("/{id}")
    public Result<ShiftConfigDTO> getById(
            @ApiParam("班次ID") @PathVariable @NotNull(message = "班次ID不能为空") Long id) {
        ShiftConfigDTO dto = shiftConfigService.getDtoById(id);
        if (dto == null) {
            return Result.error("班次不存在");
        }
        return Result.success(dto);
    }

    /**
     * 新增班次配置
     */
    @ApiOperation("新增班次配置")
    @PostMapping
    public Result<Void> save(@Valid @RequestBody ShiftConfigDTO dto) {
        boolean success = shiftConfigService.saveDto(dto);
        return success ? Result.success("新增成功", null) : Result.error("新增失败");
    }

    /**
     * 更新班次配置
     */
    @ApiOperation("更新班次配置")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody ShiftConfigDTO dto) {
        if (dto.getId() == null) {
            return Result.error("班次ID不能为空");
        }
        boolean success = shiftConfigService.updateDto(dto);
        return success ? Result.success("更新成功", null) : Result.error("更新失败");
    }

    /**
     * 删除班次配置
     */
    @ApiOperation("删除班次配置")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @ApiParam("班次ID") @PathVariable @NotNull(message = "班次ID不能为空") Long id) {
        boolean success = shiftConfigService.deleteDto(id);
        return success ? Result.success("删除成功", null) : Result.error("删除失败");
    }
}
