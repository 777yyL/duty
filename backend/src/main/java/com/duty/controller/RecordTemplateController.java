package com.duty.controller;

import com.duty.common.Result;
import com.duty.dto.RecordTemplateDTO;
import com.duty.service.RecordTemplateService;
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
 * 记录模板配置Controller
 *
 * @author duty-system
 * @since 2025-12-26
 */
@Api(tags = "记录模板配置管理")
@Validated
@RestController
@RequestMapping("/template")
public class RecordTemplateController {

    @Autowired
    private RecordTemplateService recordTemplateService;

    /**
     * 获取所有启用的模板配置
     */
    @ApiOperation("获取所有启用的模板配置")
    @GetMapping("/list/enabled")
    public Result<List<RecordTemplateDTO>> listEnabled() {
        List<RecordTemplateDTO> list = recordTemplateService.listAllEnabled();
        return Result.success(list);
    }

    /**
     * 根据ID查询模板配置
     */
    @ApiOperation("根据ID查询模板配置")
    @GetMapping("/{id}")
    public Result<RecordTemplateDTO> getById(
            @ApiParam("模板ID") @PathVariable @NotNull(message = "模板ID不能为空") Long id) {
        RecordTemplateDTO dto = recordTemplateService.getDtoById(id);
        if (dto == null) {
            return Result.error("模板不存在");
        }
        return Result.success(dto);
    }

    /**
     * 新增模板配置
     */
    @ApiOperation("新增模板配置")
    @PostMapping
    public Result<Void> save(@Valid @RequestBody RecordTemplateDTO dto) {
        boolean success = recordTemplateService.saveDto(dto);
        return success ? Result.success("新增成功", null) : Result.error("新增失败");
    }

    /**
     * 更新模板配置
     */
    @ApiOperation("更新模板配置")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody RecordTemplateDTO dto) {
        if (dto.getId() == null) {
            return Result.error("模板ID不能为空");
        }
        boolean success = recordTemplateService.updateDto(dto);
        return success ? Result.success("更新成功", null) : Result.error("更新失败");
    }

    /**
     * 删除模板配置
     */
    @ApiOperation("删除模板配置")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @ApiParam("模板ID") @PathVariable @NotNull(message = "模板ID不能为空") Long id) {
        boolean success = recordTemplateService.deleteDto(id);
        return success ? Result.success("删除成功", null) : Result.error("删除失败");
    }
}
