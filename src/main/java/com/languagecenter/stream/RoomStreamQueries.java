package com.languagecenter.stream;

import com.languagecenter.model.Room;
import com.languagecenter.model.enums.RoomStatus;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public final class RoomStreamQueries {

    private RoomStreamQueries(){}

    private static String safeLower(String s){
        return s == null ? "" : s.toLowerCase(Locale.ROOT);
    }

    public static List<Room> searchByName(List<Room> rooms,String keyword){

        String k = safeLower(keyword).trim();

        return rooms.stream()
                .filter(r -> safeLower(r.getRoomName()).contains(k))
                .collect(Collectors.toList());
    }

    public static List<Room> filterByStatus(List<Room> rooms, RoomStatus status){

        return rooms.stream()
                .filter(r -> r.getStatus() == status)
                .collect(Collectors.toList());
    }
}