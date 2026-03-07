package com.languagecenter.stream;

import com.languagecenter.model.Schedule;
import com.languagecenter.model.Student;
import com.languagecenter.model.enums.StudentStatus;

import java.time.LocalDate;
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

    /**
     * Lọc theo ngày
     */
    public static List<Schedule> filterByDate(List<Schedule> data, LocalDate date){

        if(date == null)
            return data;

        return data.stream()
                .filter(s -> s.getStudyDate().equals(date))
                .collect(Collectors.toList());
    }

    /**
     * Lọc theo khoảng ngày
     */
    public static List<Schedule> filterByDateRange(
            List<Schedule> data,
            LocalDate from,
            LocalDate to){

        if(from == null || to == null)
            return data;

        return data.stream()
                .filter(s ->
                        !s.getStudyDate().isBefore(from)
                                && !s.getStudyDate().isAfter(to)
                )
                .collect(Collectors.toList());
    }

    /**
     * Lọc lịch hôm nay
     */
    public static List<Schedule> filterToday(List<Schedule> data){

        LocalDate today = LocalDate.now();

        return data.stream()
                .filter(s -> s.getStudyDate().equals(today))
                .collect(Collectors.toList());
    }

}
