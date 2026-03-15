package com.languagecenter.service;

import com.languagecenter.db.TransactionManager;
import com.languagecenter.model.Room;
import com.languagecenter.repo.RoomRepository;

import java.util.List;

/**
 * Điều phối dữ liệu Phòng Học (Room).
 * <p>
 * Class thể hiện tính duy nhất trong trách nhiệm của SOLID (SRP), đóng vai trò
 * là đối tượng giao tiếp xử lý các nghiệp vụ riêng biệt của phòng.
 * </p>
 */
public class RoomService {

    private final RoomRepository roomRepo;
    private final TransactionManager tx;

    /**
     * Khởi tạo service bằng việc inject các dependencies liên quan.
     *
     */
    public RoomService(RoomRepository roomRepo,
                       TransactionManager tx){
        this.roomRepo = roomRepo;
        this.tx = tx;
    }

    /**
     * Trả về toàn bộ danh sách các phòng học của hệ thống.
     *
     */
    public List<Room> getAll() throws Exception {
        return tx.runInTransaction(em -> roomRepo.findAll(em));
    }

    /**
     * Khởi tạo phòng học mới.
     *
     */
    public void create(Room room) throws Exception {
        tx.runInTransaction(em -> {
            roomRepo.create(em,room);
            return null;
        });
    }

    /**
     * Cập nhật thông tin phòng thiết bị mới.
     *
     */
    public void update(Room room) throws Exception {
        tx.runInTransaction(em -> {
            roomRepo.update(em,room);
            return null;
        });
    }

    /**
     * Xóa phòng học.
     *
     */
    public void delete(Long id) throws Exception {
        tx.runInTransaction(em -> {
            roomRepo.delete(em,id);
            return null;
        });
    }
}