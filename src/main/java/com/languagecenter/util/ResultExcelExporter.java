package com.languagecenter.util;

import com.languagecenter.model.Result;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

/**
 * Exports a list of {@link Result} objects to an Excel (.xlsx) file.
 * Results are sorted alphabetically by student name before writing.
 */
public class ResultExcelExporter {

    private ResultExcelExporter() {}

    /**
     * Exports results to an Excel file.
     *
     * @param results   list of results (will be sorted by student name internally)
     * @param className display name used in the title row
     * @param filePath  absolute path of the output .xlsx file
     * @throws IOException if writing fails
     */
    public static void export(List<Result> results, String className, String filePath) throws IOException {

        // Sort alphabetically by student name
        List<Result> sorted = results.stream()
                .sorted(Comparator.comparing(
                        r -> r.getStudent() != null ? r.getStudent().getFullName() : "",
                        String.CASE_INSENSITIVE_ORDER))
                .toList();

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Results");

            // ── Styles ──────────────────────────────────────────────────────
            CellStyle titleStyle = wb.createCellStyle();
            Font titleFont = wb.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.CORNFLOWER_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            setBorder(headerStyle);

            CellStyle dataStyle = wb.createCellStyle();
            dataStyle.setAlignment(HorizontalAlignment.CENTER);
            setBorder(dataStyle);

            CellStyle dataLeftStyle = wb.createCellStyle();
            dataLeftStyle.setAlignment(HorizontalAlignment.LEFT);
            setBorder(dataLeftStyle);

            // ── Title ────────────────────────────────────────────────────────
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("KẾT QUẢ HỌC TẬP - LỚP: " + className);
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));

            // ── Header ───────────────────────────────────────────────────────
            String[] headers = {"STT", "Mã KQ", "Họ và tên học viên", "Điểm", "Xếp loại", "Nhận xét"};
            Row headerRow = sheet.createRow(1);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // ── Data rows ────────────────────────────────────────────────────
            for (int i = 0; i < sorted.size(); i++) {
                Result r = sorted.get(i);
                Row row = sheet.createRow(i + 2);

                createCell(row, 0, i + 1, dataStyle);
                createCell(row, 1, r.getId() != null ? "RS-" + r.getId() : "", dataStyle);
                createCell(row, 2, r.getStudent() != null ? r.getStudent().getFullName() : "", dataLeftStyle);
                createCell(row, 3, r.getScore() != null ? r.getScore().toPlainString() : "—", dataStyle);
                createCell(row, 4, r.getGrade() != null ? r.getGrade() : "—", dataStyle);
                createCell(row, 5, r.getComment() != null ? r.getComment() : "", dataLeftStyle);
            }

            // ── Column widths ─────────────────────────────────────────────────
            sheet.setColumnWidth(0, 8  * 256);   // STT
            sheet.setColumnWidth(1, 12 * 256);   // Mã KQ
            sheet.setColumnWidth(2, 30 * 256);   // Họ và tên
            sheet.setColumnWidth(3, 10 * 256);   // Điểm
            sheet.setColumnWidth(4, 12 * 256);   // Xếp loại
            sheet.setColumnWidth(5, 40 * 256);   // Nhận xét

            // ── Write file ────────────────────────────────────────────────────
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                wb.write(fos);
            }
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private static void createCell(Row row, int col, Object value, CellStyle style) {
        Cell cell = row.createCell(col);
        if (value instanceof Number n)
            cell.setCellValue(n.doubleValue());
        else
            cell.setCellValue(value != null ? value.toString() : "");
        cell.setCellStyle(style);
    }

    private static void setBorder(CellStyle style) {
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }
}
