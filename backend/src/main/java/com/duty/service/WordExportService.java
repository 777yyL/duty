package com.duty.service;

import com.duty.dto.DutyRecordDTO;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

/**
 * Word导出Service接口
 *
 * @author duty-system
 * @since 2025-12-26
 */
public interface WordExportService {

    /**
     * 导出单条值班记录为Word
     *
     * @param recordId 记录ID
     * @param response HTTP响应
     */
    void exportSingleRecord(Long recordId, HttpServletResponse response);

    /**
     * 导出某个人的值班记录（可按时间段筛选）
     *
     * @param personId 人员ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param response HTTP响应
     */
    void exportPersonRecords(String personId, LocalDate startDate, LocalDate endDate, HttpServletResponse response);

    /**
     * 批量导出值班记录为Word
     *
     * @param recordIds 记录ID列表
     * @param response HTTP响应
     */
    void exportBatchRecords(Long[] recordIds, HttpServletResponse response);
}
