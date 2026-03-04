package com.languagecenter.stream;

import com.languagecenter.model.Student;
import com.languagecenter.model.enums.StudentStatus;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Ví dụ các query dùng Java Stream cho Student.
 * Input: List<Student>
 * Output: List<Student> / Map / statistics
 */
public final class StudentStreamQueries {
    private StudentStreamQueries() {}

    private static String safeLower(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT);
    }

    public static List<Student> searchByName(List<Student> students, String keyword) {
        String k = safeLower(keyword).trim();

        return students.stream()
                .filter(s -> safeLower(s.getFullName()).contains(k))
                .collect(Collectors.toList());
    }

    public static List<Student> filterByStatus(List<Student> students, StudentStatus status) {
        return students.stream()
                .filter(s -> s.getStatus() == status)
                .collect(Collectors.toList());
    }
}
