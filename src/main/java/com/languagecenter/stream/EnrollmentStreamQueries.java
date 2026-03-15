package com.languagecenter.stream;

import com.languagecenter.model.Enrollment;

import java.util.List;
import java.util.stream.Collectors;

public class EnrollmentStreamQueries {

    /**
     * Lọc danh sách đăng ký theo tên học sinh.
     */
    public static List<Enrollment> filterByStudent(List<Enrollment> data,String keyword){

        if(keyword == null || keyword.isBlank())
            return data;

        return data.stream()
                .filter(e -> e.getStudent().getFullName()
                        .toLowerCase()
                        .contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * Lọc danh sách đăng ký theo tên lớp học.
     */
    public static List<Enrollment> filterByClass(List<Enrollment> data,String keyword){

        if(keyword == null || keyword.isBlank())
            return data;

        return data.stream()
                .filter(e -> e.getClassEntity().getClassName()
                        .toLowerCase()
                        .contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
    }
}