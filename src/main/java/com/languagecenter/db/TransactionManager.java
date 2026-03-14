package com.languagecenter.db;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

/**
 * Lớp quản lý giao dịch CSDL (Transaction Manager).
 * Chịu trách nhiệm bao bọc các thao tác JPA trong một giao dịch an toàn (begin, commit, và rollback tự động).
 * Phù hợp với nguyên lý Single Responsibility (Mỗi lớp một trách nhiệm duy nhất).
 */
public class TransactionManager {

    /**
     * Chạy một tác vụ có trả về kết quả trong một giao dịch (transaction) được tự động quản lý.
     * Tự động bắt lỗi lấy lại trạng thái rollback và đóng EntityManager an toàn.
     *
     * @param <T> Kiểu biểu diễn của kết quả mong đợi truyền về.
     * @param work Khối công việc JpaWork cần thực thi.
     * @return Giá trị trả về từ kết quả công việc cung cấp.
     * @throws Exception khi các tác vụ nội tại báo lỗi về giao dịch hoặc logic.
     */
    public <T> T runInTransaction(JpaWork<T> work) throws Exception {
        EntityManager em = Jpa.em();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            T result = work.execute(em);
            tx.commit();
            return result;
        } catch (Exception ex) {
            if (tx.isActive()) tx.rollback();
            throw ex;
        } finally {
            em.close();
        }
    }
}
