package com.languagecenter.repo.jpa;

import com.languagecenter.model.UserAccount;
import com.languagecenter.repo.UserAccountRepository;
import jakarta.persistence.EntityManager;

public class JpaUserAccountRepository implements UserAccountRepository {

    @Override
    public UserAccount findByUsername(EntityManager em,String username){

        return em.createQuery(
                        "select u from UserAccount u where u.username=:username",
                        UserAccount.class
                )
                .setParameter("username",username)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public void save(EntityManager em,UserAccount user){
        em.persist(user);
    }

    @Override
    public UserAccount findByStudentId(EntityManager em, Long studentId) {
        return em.createQuery(
                        "select u from UserAccount u where u.student.id = :sid",
                        UserAccount.class
                )
                .setParameter("sid", studentId)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public void delete(EntityManager em, Long id) {
        UserAccount acc = em.find(UserAccount.class, id);
        if (acc != null) {
            em.remove(acc);
        }
    }
}