package com.languagecenter.stream;

import com.languagecenter.model.Schedule;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public final class ScheduleStreamQueries {

    private ScheduleStreamQueries(){}

    public static List<Schedule> filterByDate(List<Schedule> data, LocalDate date){
        if(date == null) return data;

        return data.stream()
                .filter(s -> s.getStudyDate().equals(date))
                .collect(Collectors.toList());
    }

    public static List<Schedule> filterByClass(List<Schedule> data, String keyword){
        if(keyword == null || keyword.isBlank()) return data;

        return data.stream()
                .filter(s -> s.getClassEntity().getClassName().toLowerCase()
                        .contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
    }
}