package com.languagecenter.db;

import jakarta.persistence.EntityManager;

/**
 * Giao diện chức năng biểu diễn một bản làm việc với JPA. 
 * Hỗ trợ cho các thao tác và tác vụ xử lý trên EntityManager có thể cần thực thi ngoại lệ.
 *
 * @param <T> Kiểu dữ liệu trả về của tác vụ.
 */
@FunctionalInterface
public interface JpaWork<T> {
    
    /**
     * Thực thi tác vụ lấy EntityManager.
     *
     * @param em EntityManager được sử dụng để tương tác với CSDL.
     * @return Kết quả của tác vụ, với kiểu T.
     * @throws Exception trong trường hợp tác vụ bị lỗi.
     */
    T execute(EntityManager em) throws Exception;
}
