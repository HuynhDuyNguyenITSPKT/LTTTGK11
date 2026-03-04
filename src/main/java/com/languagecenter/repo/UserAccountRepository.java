package com.languagecenter.repo;

import com.languagecenter.model.UserAccount;
import jakarta.persistence.EntityManager;

public interface UserAccountRepository {
    UserAccount findByUsername(EntityManager em, String username) throws Exception;
    void save(EntityManager em,UserAccount user) throws Exception;
    UserAccount findByStudentId(EntityManager em, Long studentId);
    void delete(EntityManager em, Long id);
}
