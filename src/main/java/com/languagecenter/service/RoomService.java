package com.languagecenter.service;

import com.languagecenter.db.TransactionManager;
import com.languagecenter.model.Room;
import com.languagecenter.repo.RoomRepository;

import java.util.List;

public class RoomService {

    private final RoomRepository roomRepo;
    private final TransactionManager tx;

    public RoomService(RoomRepository roomRepo,
                       TransactionManager tx){

        this.roomRepo = roomRepo;
        this.tx = tx;
    }

    public List<Room> getAll() throws Exception {

        return tx.runInTransaction(
                em -> roomRepo.findAll(em)
        );
    }

    public void create(Room room) throws Exception {

        tx.runInTransaction(em -> {

            roomRepo.create(em,room);

            return null;
        });
    }

    public void update(Room room) throws Exception {

        tx.runInTransaction(em -> {

            roomRepo.update(em,room);

            return null;
        });
    }

    public void delete(Long id) throws Exception {

        tx.runInTransaction(em -> {

            roomRepo.delete(em,id);

            return null;
        });
    }
}