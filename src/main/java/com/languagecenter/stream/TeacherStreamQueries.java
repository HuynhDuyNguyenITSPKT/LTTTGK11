package com.languagecenter.stream;

import com.languagecenter.model.Teacher;
import com.languagecenter.model.enums.TeacherStatus;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Các thao tác lọc và tìm kiếm cho Giáo viên.
 */
public final class TeacherStreamQueries {

    private TeacherStreamQueries() {}

    private static String safeLower(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT);
    }

    /**
     * Tìm kiếm giáo viên theo tên.
     */
    public static List<Teacher> searchByName(List<Teacher> teachers, String keyword) {
        String k = safeLower(keyword).trim();
        return teachers.stream()
                .filter(t -> safeLower(t.getFullName()).contains(k))
                .collect(Collectors.toList());
    }

    /**
     * Lọc giáo viên theo trạng thái.
     */
    public static List<Teacher> filterByStatus(List<Teacher> teachers, TeacherStatus status) {

        return teachers.stream()
                .filter(t -> t.getStatus() == status)
                .collect(Collectors.toList());
    }

    /**
     * Lọc giáo viên theo chuyên môn.
     */
    public static List<Teacher> filterBySpecialty(List<Teacher> teachers, String keyword) {
        String k = safeLower(keyword).trim();
        return teachers.stream()
                .filter(t -> safeLower(t.getSpecialty()).contains(k))
                .collect(Collectors.toList());
    }

}