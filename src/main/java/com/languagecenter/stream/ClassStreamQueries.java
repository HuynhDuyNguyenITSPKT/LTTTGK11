package com.languagecenter.stream;

import com.languagecenter.model.Class;
import com.languagecenter.model.enums.ClassStatus;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public final class ClassStreamQueries {

    private ClassStreamQueries() {}

    private static String safeLower(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT);
    }

    /**
     * Tìm kiếm theo tên lớp học (Không phân biệt hoa thường)
     */
    public static List<Class> searchByName(List<Class> classes, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return classes;
        String k = safeLower(keyword).trim();
        return classes.stream()
                .filter(c -> safeLower(c.getClassName()).contains(k))
                .collect(Collectors.toList());
    }

    /**
     * Lọc theo trạng thái lớp học (Planned, Open, Ongoing, v.v.)
     */
    public static List<Class> filterByStatus(List<Class> classes, ClassStatus status) {
        if (status == null) return classes;
        return classes.stream()
                .filter(c -> c.getStatus() == status)
                .collect(Collectors.toList());
    }

    /**
     * Lọc theo học phí của khóa học (Course fee)
     */
    public static List<Class> filterByCourseFee(List<Class> classes, Double minFee, Double maxFee) {
        return classes.stream()
                .filter(c -> c.getCourse() != null) // Đảm bảo lớp đã gán khóa học
                .filter(c -> {
                    Double fee = c.getCourse().getFee();
                    boolean matchMin = (minFee == null || fee >= minFee);
                    boolean matchMax = (maxFee == null || fee <= maxFee);
                    return matchMin && matchMax;
                })
                .collect(Collectors.toList());
    }
}