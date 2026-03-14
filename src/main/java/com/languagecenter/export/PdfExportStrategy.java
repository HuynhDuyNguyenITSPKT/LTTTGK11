package com.languagecenter.export;

import com.languagecenter.model.Student;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.io.FileOutputStream;
import java.util.List;

public class PdfExportStrategy implements ExportStrategy {

    @Override
    public void export(List<Student> students, String filePath) throws Exception {

        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(filePath));

        document.open();

        PdfPTable table = new PdfPTable(7);

        table.addCell("ID");
        table.addCell("Full Name");
        table.addCell("Gender");
        table.addCell("Phone");
        table.addCell("Email");
        table.addCell("Address");
        table.addCell("Status");

        for (Student s : students) {

            table.addCell(String.valueOf(s.getId()));
            table.addCell(s.getFullName());
            table.addCell(String.valueOf(s.getGender()));
            table.addCell(s.getPhone());
            table.addCell(s.getEmail());
            table.addCell(s.getAddress());
            table.addCell(String.valueOf(s.getStatus()));
        }

        document.add(table);
        document.close();
    }
}