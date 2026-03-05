package com.languagecenter.repo;

import com.languagecenter.model.Room;
import jakarta.persistence.EntityManager;

import java.util.List;

public interface RoomRepository {

    List<Room> findAll(EntityManager em) throws Exception;

    Room findById(EntityManager em, Long id) throws Exception;

    void create(EntityManager em, Room room) throws Exception;

    void update(EntityManager em, Room room) throws Exception;

    void delete(EntityManager em, Long id) throws Exception;
}