package com.languagecenter.stream;

import com.languagecenter.model.Result;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

public class ResultStreamQueries {

    private ResultStreamQueries() {}

    /** Lọc kết quả theo tên học sinh. */
    public static List<Result> filterByStudent(List<Result> data, String keyword) {
        String kw = keyword.toLowerCase();
        return data.stream()
                .filter(r -> r.getStudent() != null &&
                             r.getStudent().getFullName().toLowerCase().contains(kw))
                .toList();
    }

    /** Lọc kết quả theo tên lớp học. */
    public static List<Result> filterByClass(List<Result> data, String keyword) {
        String kw = keyword.toLowerCase();
        return data.stream()
                .filter(r -> r.getClassEntity() != null &&
                             r.getClassEntity().getClassName().toLowerCase().contains(kw))
                .toList();
    }

    /** Lọc kết quả theo mã lớp học. */
    public static List<Result> filterByClassId(List<Result> data, Long classId) {
        return data.stream()
                .filter(r -> r.getClassEntity() != null &&
                             r.getClassEntity().getId().equals(classId))
                .toList();
    }

    /** Lọc kết quả theo đánh giá. */
    public static List<Result> filterByGrade(List<Result> data, String grade) {
        String g = grade.toLowerCase();
        return data.stream()
                .filter(r -> r.getGrade() != null && r.getGrade().toLowerCase().equals(g))
                .toList();
    }

    /** Lọc kết quả theo điểm số tối thiểu. */
    public static List<Result> filterByMinScore(List<Result> data, BigDecimal min) {
        return data.stream()
                .filter(r -> r.getScore() != null && r.getScore().compareTo(min) >= 0)
                .toList();
    }

    /** Lọc kết quả theo khoảng điểm số. */
    public static List<Result> filterByScoreRange(List<Result> data, BigDecimal min, BigDecimal max) {
        return data.stream()
                .filter(r -> r.getScore() != null
                        && r.getScore().compareTo(min) >= 0
                        && r.getScore().compareTo(max) <= 0)
                .toList();
    }

    /** Sắp xếp kết quả theo tên học sinh từ A-Z. */
    public static List<Result> sortByStudentName(List<Result> data) {
        return data.stream()
                .sorted(Comparator.comparing(
                        r -> r.getStudent() != null ? r.getStudent().getFullName() : "",
                        String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    /** Tính điểm trung bình của danh sách kết quả. */
    public static BigDecimal averageScore(List<Result> data) {
        return data.stream()
                .filter(r -> r.getScore() != null)
                .map(Result::getScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(
                    BigDecimal.valueOf(Math.max(1, data.stream().filter(r -> r.getScore() != null).count())),
                    2, java.math.RoundingMode.HALF_UP
                );
    }
}
