package com.languagecenter.export;

import com.languagecenter.model.Student;
import java.util.List;

public class ExportService {

    private ExportStrategy strategy;

    public ExportService(ExportStrategy strategy) {
        this.strategy = strategy;
    }

    public void export(List<Student> students, String filePath) throws Exception {
        strategy.export(students, filePath);
    }
}