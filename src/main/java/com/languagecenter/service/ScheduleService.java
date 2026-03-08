package com.languagecenter.service;

import com.languagecenter.db.TransactionManager;
import com.languagecenter.model.Class;
import com.languagecenter.model.Schedule;
import com.languagecenter.repo.ScheduleRepository;

import java.util.List;

public class ScheduleService {

    private final ScheduleRepository repo;
    private final TransactionManager tx;

    public ScheduleService(ScheduleRepository repo, TransactionManager tx) {
        this.repo = repo;
        this.tx = tx;
    }

    public List<Schedule> getAll() throws Exception {
        return tx.runInTransaction(repo::findAll);
    }

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
            return null;
        });
    }

    public void update(Schedule schedule) throws Exception {
        tx.runInTransaction(em -> {
            repo.update(em, schedule);
            return null;
        });
    }

    public void delete(Long id) throws Exception {
        tx.runInTransaction(em -> {
            repo.delete(em, id);
            return null;
        });
    }

    public List<Schedule> getScheduleByStudent(Long studentId) throws Exception {

        return tx.runInTransaction(
                em -> repo.getScheduleByStudent(em, studentId)
        );
    }

    public List<Schedule> getScheduleByTeacher(Long teacherId) throws Exception {

        return tx.runInTransaction(
                em -> repo.getScheduleByTeacher(em, teacherId)
        );
    }


}