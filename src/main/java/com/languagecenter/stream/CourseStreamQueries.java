package com.languagecenter.stream;

import com.languagecenter.model.Course;
import com.languagecenter.model.enums.CourseStatus;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public final class CourseStreamQueries {

    private CourseStreamQueries(){}

    private static String safeLower(String s){
        return s==null ? "" : s.toLowerCase(Locale.ROOT);
    }

    public static List<Course> searchByName(List<Course> courses,String keyword){

        String k = safeLower(keyword).trim();

        return courses.stream()
                .filter(c-> safeLower(c.getCourseName()).contains(k))
                .collect(Collectors.toList());
    }

    public static List<Course> filterByStatus(List<Course> courses, CourseStatus status){

        return courses.stream()
                .filter(c-> c.getStatus()==status)
                .collect(Collectors.toList());
    }
}