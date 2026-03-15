package com.languagecenter.service;

import com.languagecenter.db.TransactionManager;
import com.languagecenter.model.Class;
import com.languagecenter.model.Schedule;
import com.languagecenter.repo.ScheduleRepository;
import jakarta.transaction.Transactional;

import java.util.List;

/**
 * Điều phối thông tin đăng ký lịch học, kiểm tra điều kiện trùng, tạo mới.
 * <p>
 * Class này bảo đảm nguyên tắc SRP (Single Responsibility Principle) bằng việc
 * phụ trách độc lập các logic xử lý liên đới đến lịch học (Schedule).
 * </p>
 */
public class ScheduleService {

    private final ScheduleRepository repo;
    private final TransactionManager tx;

    /**
     * Khởi tạo tính năng xử lý lịch học.
     *
     */
    public ScheduleService(ScheduleRepository repo, TransactionManager tx) {
        this.repo = repo;
        this.tx = tx;
    }

    /**
     * Lấy toàn bộ danh sách lịch học hiện tại.
     *
     */
    public List<Schedule> getAll() throws Exception {
        return tx.runInTransaction(repo::findAll);
    }

    /**
     * Cung cấp logic kiểm tra trùng lịch (phòng, giáo viên, thời gian) 
     * trước khi khởi tạo lịch học.
     *
     */
    @Transactional
    public void create(Schedule schedule) throws Exception {
        tx.runInTransaction(em -> {
            if(schedule.getEndTime().isBefore(schedule.getStartTime()))
                throw new Exception("Giờ kết thúc phải sau giờ bắt đầu!");

            Class clazz = schedule.getClassEntity();

            if(schedule.getStudyDate().isBefore(clazz.getStartDate()) ||
                    schedule.getStudyDate().isAfter(clazz.getEndDate()))
                throw new Exception("Ngày học phải nằm trong thời gian lớp!");

            boolean conflict = repo.existsRoomConflict(
                    em,
                    schedule.getRoom().getId(),
                    schedule.getStudyDate(),
                    schedule.getStartTime(),
                    schedule.getEndTime()
            );

            if(conflict)
                throw new Exception("Phòng đã có lịch trong khung giờ này!");

            repo.create(em, schedule);

            boolean conflict1 = repo.checkTeacherScheduleConflict(em);

            if(!conflict1)  throw new Exception("Giáo viên đã có lịch trong khung giờ này!");

            return null;
        });
    }

    /**
     * Cập nhật thời điểm lịch học bị thay đổi.
     *
     */
    public void update(Schedule schedule) throws Exception {
        tx.runInTransaction(em -> {
            repo.update(em, schedule);
            return null;
        });
    }

    /**
     * Hủy hoặc xóa sự kiện lịch học.
     *
     */
    public void delete(Long id) throws Exception {
        tx.runInTransaction(em -> {
            repo.delete(em, id);
            return null;
        });
    }

    /**
     * Lấy danh sách thời khóa biểu đặc thù của người học.
     *
     */
    public List<Schedule> getScheduleByStudent(Long studentId) throws Exception {
        return tx.runInTransaction(em -> repo.getScheduleByStudent(em, studentId));
    }

    /**
     * Lấy danh sách thời khóa biểu đứng lớp cho giáo viên cụ thể.
     *
     */
    public List<Schedule> getScheduleByTeacher(Long teacherId) throws Exception {
        return tx.runInTransaction(em -> repo.getScheduleByTeacher(em, teacherId));
    }
    
    /**
     * Lọc danh sách lịch dạy dành cho từng Khóa / Lớp học riêng lẻ.
     *
     */
    public List<Schedule> getByClass(Long classId) throws Exception {
        return tx.runInTransaction(em -> repo.getByClass(em, classId));
    }
}