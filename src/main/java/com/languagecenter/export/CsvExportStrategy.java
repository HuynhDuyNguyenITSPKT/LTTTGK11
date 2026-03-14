package com.languagecenter.export;

import com.languagecenter.model.Student;
import java.io.FileWriter;
import java.util.List;

public class CsvExportStrategy implements ExportStrategy {

    @Override
    public void export(List<Student> students, String filePath) throws Exception {

        FileWriter writer = new FileWriter(filePath);

        writer.append("ID,FullName,Gender,Phone,Email,Address,Status\n");

        for (Student s : students) {
            writer.append(s.getId() + ",");
            writer.append(s.getFullName() + ",");
            writer.append(s.getGender() + ",");
            writer.append(s.getPhone() + ",");
            writer.append(s.getEmail() + ",");
            writer.append(s.getAddress() + ",");
            writer.append(String.valueOf(s.getStatus()));
            writer.append("\n");
        }

        writer.flush();
        writer.close();
    }
}