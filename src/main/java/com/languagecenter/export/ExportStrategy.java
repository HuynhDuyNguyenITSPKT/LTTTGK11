package com.languagecenter.export;

import com.languagecenter.model.Student;
import java.util.List;

public interface ExportStrategy {
    void export(List<Student> students, String filePath) throws Exception;
}