package com.languagecenter.export;

import com.languagecenter.model.Student;
import org.apache.poi.xssf.usermodel.*;
import java.io.FileOutputStream;
import java.util.List;

public class ExcelExportStrategy implements ExportStrategy {

    @Override
    public void export(List<Student> students, String filePath) throws Exception {

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Students");

        int rowNum = 0;

        XSSFRow header = sheet.createRow(rowNum++);
        header.createCell(0).setCellValue("ID");
        header.createCell(1).setCellValue("Full Name");
        header.createCell(2).setCellValue("Gender");
        header.createCell(3).setCellValue("Phone");
        header.createCell(4).setCellValue("Email");
        header.createCell(5).setCellValue("Address");
        header.createCell(6).setCellValue("Status");

        for (Student s : students) {

            XSSFRow row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(s.getId());
            row.createCell(1).setCellValue(s.getFullName());
            row.createCell(2).setCellValue(String.valueOf(s.getGender()));
            row.createCell(3).setCellValue(s.getPhone());
            row.createCell(4).setCellValue(s.getEmail());
            row.createCell(5).setCellValue(s.getAddress());
            row.createCell(6).setCellValue(String.valueOf(s.getStatus()));
        }

        FileOutputStream out = new FileOutputStream(filePath);
        workbook.write(out);

        out.close();
        workbook.close();
    }
}