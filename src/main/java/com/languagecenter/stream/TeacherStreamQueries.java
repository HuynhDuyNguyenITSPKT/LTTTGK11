package com.languagecenter.stream;

import com.languagecenter.model.Teacher;
import com.languagecenter.model.enums.TeacherStatus;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Ví dụ các query dùng Java Stream cho Teacher.
 * Input: List<Teacher>
 * Output: List<Teacher>
 */
public final class TeacherStreamQueries {

    private TeacherStreamQueries() {}

    private static String safeLower(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT);
    }

    /**
     * Search teacher by name (contains, ignore case)
     */
    public static List<Teacher> searchByName(List<Teacher> teachers, String keyword) {
        String k = safeLower(keyword).trim();
        return teachers.stream()
                .filter(t -> safeLower(t.getFullName()).contains(k))
                .collect(Collectors.toList());
    }

    /**
     * Filter teacher by status
     */
    public static List<Teacher> filterByStatus(List<Teacher> teachers, TeacherStatus status) {

        return teachers.stream()
                .filter(t -> t.getStatus() == status)
                .collect(Collectors.toList());
    }

    /**
     * Filter teacher by specialty
     */
    public static List<Teacher> filterBySpecialty(List<Teacher> teachers, String keyword) {
        String k = safeLower(keyword).trim();
        return teachers.stream()
                .filter(t -> safeLower(t.getSpecialty()).contains(k))
                .collect(Collectors.toList());
    }

}