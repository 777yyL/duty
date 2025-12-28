package com.duty.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.duty.dto.DutyRecordDTO;
import com.duty.dto.DutyRecordDetailDTO;
import com.duty.service.DutyRecordService;
import com.duty.service.WordExportService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * Word导出Service实现类
 *
 * @author duty-system
 * @since 2025-12-26
 */
@Slf4j
@Service
public class WordExportServiceImpl implements WordExportService {

    @Autowired
    private DutyRecordService dutyRecordService;

    @Override
    public void exportSingleRecord(Long recordId, HttpServletResponse response) {
        try {
            DutyRecordDTO record = dutyRecordService.getDetailById(recordId);
            if (record == null) {
                throw new RuntimeException("值班记录不存在");
            }

            // 创建Word文档
            XWPFDocument document = new XWPFDocument();

            // 设置文档标题
            createTitle(document, "值班记录");

            // 创建基本信息表格
            createBasicInfoTable(document, record);

            // 创建工作日志表格
            createWorkLogTable(document, record);

            // 设置响应头
            String fileName = "值班记录_" + record.getPersonName() + "_" + record.getDutyDate() + ".docx";
            setResponseHeader(response, fileName);

            // 写入响应流
            OutputStream os = response.getOutputStream();
            document.write(os);
            os.flush();
            os.close();
            document.close();

        } catch (Exception e) {
            log.error("导出Word失败", e);
            throw new RuntimeException("导出Word失败: " + e.getMessage());
        }
    }

    @Override
    public void exportPersonRecords(String personId, LocalDate startDate, LocalDate endDate, HttpServletResponse response) {
        try {
            // 查询该人员的所有值班记录
            List<DutyRecordDTO> records = dutyRecordService.pageList(
                    1L, 1000L, personId, null, startDate, endDate
            ).getRecords();

            if (CollUtil.isEmpty(records)) {
                throw new RuntimeException("该时间段内没有值班记录");
            }

            // 创建Word文档
            XWPFDocument document = new XWPFDocument();

            // 设置文档标题
            String personName = records.get(0).getPersonName();
            createTitle(document, personName + " - 值班记录汇总");

            // 遍历每条记录
            for (int i = 0; i < records.size(); i++) {
                DutyRecordDTO record = records.get(i);

                // 创建分页（除了第一条）
                if (i > 0) {
                    createPageBreak(document);
                }

                // 记录标题
                createRecordTitle(document, "第 " + (i + 1) + " 条记录 - " + record.getDutyDate());

                // 创建基本信息表格
                createBasicInfoTable(document, record);

                // 创建工作日志表格
                createWorkLogTable(document, record);
            }

            // 设置响应头
            String fileName = personName + "_值班记录_" + startDate + "_to_" + endDate + ".docx";
            setResponseHeader(response, fileName);

            // 写入响应流
            OutputStream os = response.getOutputStream();
            document.write(os);
            os.flush();
            os.close();
            document.close();

        } catch (Exception e) {
            log.error("导出Word失败", e);
            throw new RuntimeException("导出Word失败: " + e.getMessage());
        }
    }

    @Override
    public void exportBatchRecords(Long[] recordIds, HttpServletResponse response) {
        try {
            // 查询记录
            List<DutyRecordDTO> records = CollUtil.newArrayList();
            for (Long id : recordIds) {
                DutyRecordDTO record = dutyRecordService.getDetailById(id);
                if (record != null) {
                    records.add(record);
                }
            }

            if (CollUtil.isEmpty(records)) {
                throw new RuntimeException("没有找到有效的值班记录");
            }

            // 创建Word文档
            XWPFDocument document = new XWPFDocument();

            // 设置文档标题
            createTitle(document, "值班记录汇总");

            // 遍历每条记录
            for (int i = 0; i < records.size(); i++) {
                DutyRecordDTO record = records.get(i);

                // 创建分页（除了第一条）
                if (i > 0) {
                    createPageBreak(document);
                }

                // 记录标题
                createRecordTitle(document, "记录 " + (i + 1) + " - " + record.getPersonName() + " (" + record.getDutyDate() + ")");

                // 创建基本信息表格
                createBasicInfoTable(document, record);

                // 创建工作日志表格
                createWorkLogTable(document, record);
            }

            // 设置响应头
            String fileName = "值班记录_批量导出_" + System.currentTimeMillis() + ".docx";
            setResponseHeader(response, fileName);

            // 写入响应流
            OutputStream os = response.getOutputStream();
            document.write(os);
            os.flush();
            os.close();
            document.close();

        } catch (Exception e) {
            log.error("导出Word失败", e);
            throw new RuntimeException("导出Word失败: " + e.getMessage());
        }
    }

    /**
     * 创建文档标题
     */
    private void createTitle(XWPFDocument document, String text) {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.CENTER);

        XWPFRun run = paragraph.createRun();
        run.setText(text);
        run.setBold(true);
        run.setFontSize(18);
        run.setFontFamily("宋体");
    }

    /**
     * 创建记录标题
     */
    private void createRecordTitle(XWPFDocument document, String text) {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.LEFT);

        XWPFRun run = paragraph.createRun();
        run.setText(text);
        run.setBold(true);
        run.setFontSize(14);
        run.setFontFamily("宋体");

        // 添加空行
        document.createParagraph();
    }

    /**
     * 创建基本信息表格
     */
    private void createBasicInfoTable(XWPFDocument document, DutyRecordDTO record) {
        XWPFTable table = document.createTable(5, 2);

        // 设置表格宽度
        CTTblPr tblPr = table.getCTTbl().getTblPr();
        CTTblWidth width = tblPr.isSetTblW() ? tblPr.getTblW() : tblPr.addNewTblW();
        width.setW(BigInteger.valueOf(8000));
        width.setType(STTblWidth.DXA);

        // 填充表格数据
        setTableRow(table, 0, "值班日期:", record.getDutyDate() != null ? record.getDutyDate().toString() : "");
        setTableRow(table, 1, "值班人员:", record.getPersonName());
        setTableRow(table, 2, "值班时段:", record.getShiftStartTime() + " - " + record.getShiftEndTime());
        setTableRow(table, 3, "记录时间:", record.getRecordTime() != null ?
                DateUtil.format(Date.from(record.getRecordTime().atZone(ZoneId.systemDefault()).toInstant()), "yyyy-MM-dd HH:mm:ss") : "");
        setTableRow(table, 4, "状态:", record.getStatus() == 1 ? "正常" : "作废");

        // 添加空行
        document.createParagraph();
    }

    /**
     * 创建工作日志表格
     */
    private void createWorkLogTable(XWPFDocument document, DutyRecordDTO record) {
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setText("工作日志:");
        run.setBold(true);
        run.setFontSize(14);
        run.setFontFamily("宋体");

        document.createParagraph();

        if (CollUtil.isEmpty(record.getDetailList())) {
            // 没有明细时显示提示
            paragraph = document.createParagraph();
            run = paragraph.createRun();
            run.setText("（无工作日志记录）");
            run.setFontSize(11);
            return;
        }

        // 创建表格
        XWPFTable table = document.createTable(record.getDetailList().size() + 1, 2);

        // 设置表头
        XWPFTableRow headerRow = table.getRow(0);
        setTableCell(headerRow.getCell(0), "类别", true, "1C7F3E");
        setTableCell(headerRow.getCell(1), "内容", true, "1C7F3E");

        // 填充数据
        List<DutyRecordDetailDTO> details = record.getDetailList();
        for (int i = 0; i < details.size(); i++) {
            DutyRecordDetailDTO detail = details.get(i);
            XWPFTableRow dataRow = table.getRow(i + 1);

            setTableCell(dataRow.getCell(0), detail.getCategoryName(), false, null);
            setTableCell(dataRow.getCell(1), detail.getRecordContent() != null ? detail.getRecordContent() : "", false, null);
        }
    }

    /**
     * 设置表格行
     */
    private void setTableRow(XWPFTable table, int row, String label, String value) {
        XWPFTableRow tableRow = table.getRow(row);
        tableRow.getCell(0).setText(label);
        setTableCellBold(tableRow.getCell(0), true);

        tableRow.getCell(1).setText(value != null ? value : "");
    }

    /**
     * 设置表格单元格
     */
    private void setTableCell(XWPFTableCell cell, String text, boolean bold, String bgColor) {
        cell.setText(text);

        // 设置加粗
        if (bold) {
            setTableCellBold(cell, true);
        }

        // 设置背景色
        if (bgColor != null) {
            CTTcPr tcPr = cell.getCTTc().isSetTcPr() ? cell.getCTTc().getTcPr() : cell.getCTTc().addNewTcPr();
            CTShd shd = tcPr.isSetShd() ? tcPr.getShd() : tcPr.addNewShd();
            shd.setFill(bgColor);
        }
    }

    /**
     * 设置表格单元格加粗
     */
    private void setTableCellBold(XWPFTableCell cell, boolean bold) {
        for (XWPFParagraph p : cell.getParagraphs()) {
            for (XWPFRun r : p.getRuns()) {
                r.setBold(bold);
            }
        }
    }

    /**
     * 创建分页符
     */
    private void createPageBreak(XWPFDocument document) {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setPageBreak(true);
    }

    /**
     * 设置响应头
     */
    private void setResponseHeader(HttpServletResponse response, String fileName) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        response.setCharacterEncoding("UTF-8");

        // URL编码文件名，支持中文
        String encodedFileName = java.net.URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=UTF-8''" + encodedFileName);
    }
}
