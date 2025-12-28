package com.duty.controller;

import com.duty.service.WordExportService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Word导出Controller
 *
 * @author duty-system
 * @since 2025-12-26
 */
@Api(tags = "Word导出")
@Validated
@RestController
@RequestMapping("/export")
public class WordExportController {

    @Autowired
    private WordExportService wordExportService;

    /**
     * 导出单条值班记录
     */
    @ApiOperation("导出单条值班记录")
    @GetMapping("/record/{recordId}")
    public void exportSingleRecord(
            @ApiParam("记录ID") @PathVariable @NotNull(message = "记录ID不能为空") Long recordId,
            HttpServletResponse response) {
        wordExportService.exportSingleRecord(recordId, response);
    }

    /**
     * 导出某个人的值班记录
     */
    @ApiOperation("导出某个人的值班记录")
    @GetMapping("/person/{personId}")
    public void exportPersonRecords(
            @ApiParam("人员ID") @PathVariable @NotNull(message = "人员ID不能为空") String personId,
            @ApiParam("开始日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @ApiParam("结束日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            HttpServletResponse response) {
        wordExportService.exportPersonRecords(personId, startDate, endDate, response);
    }

    /**
     * 批量导出值班记录
     */
    @ApiOperation("批量导出值班记录")
    @PostMapping("/batch")
    public void exportBatchRecords(
            @ApiParam("记录ID列表") @RequestBody @NotNull(message = "记录ID列表不能为空") Long[] recordIds,
            HttpServletResponse response) {
        wordExportService.exportBatchRecords(recordIds, response);
    }
}
