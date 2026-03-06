package com.languagecenter.repo.jpa;

import com.languagecenter.model.Room;
import com.languagecenter.repo.RoomRepository;
import jakarta.persistence.EntityManager;

import java.util.List;

public class JpaRoomRepository implements RoomRepository {

    @Override
    public List<Room> findAll(EntityManager em) {

        return em.createQuery(
                "select r from Room r order by r.roomName",
                Room.class
        ).getResultList();
    }

    @Override
    public Room findById(EntityManager em, Long id) {

        return em.find(Room.class,id);
    }

    @Override
    public void create(EntityManager em, Room room) {

        em.persist(room);
    }

    @Override
    public void update(EntityManager em, Room room) {

        em.merge(room);
    }

    @Override
    public void delete(EntityManager em, Long id) {

        Room r = em.find(Room.class,id);

        if(r!=null){
            em.remove(r);
        }
    }
}