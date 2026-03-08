package com.languagecenter.repo.jpa;

import com.languagecenter.model.Invoice;
import com.languagecenter.repo.InvoiceRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import java.util.List;

public class JpaInvoiceRepository implements InvoiceRepository {

    @Override
    public List<Invoice> findAll(EntityManager em) {
        return em.createQuery(
                """
                select i from Invoice i
                left join fetch i.student
                left join fetch i.enrollment
                order by i.issueDate desc
                """,
                Invoice.class
        ).getResultList();
    }

    @Override
    public Invoice findById(EntityManager em, Long id) {
        return em.find(Invoice.class, id);
    }

    @Override
    public Invoice findByEnrollmentId(EntityManager em, Long enrollmentId) {
        try {
            return em.createQuery(
                    """
                    select i from Invoice i
                    where i.enrollment.id = :enrollmentId
                    """,
                    Invoice.class
            )
            .setParameter("enrollmentId", enrollmentId)
            .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Invoice> findByStudentId(EntityManager em, Long studentId) {
        return em.createQuery(
                """
                select i from Invoice i
                left join fetch i.enrollment
                where i.student.id = :studentId
                order by i.issueDate desc
                """,
                Invoice.class
        )
        .setParameter("studentId", studentId)
        .getResultList();
    }

    @Override
    public void create(EntityManager em, Invoice invoice) {
        em.persist(invoice);
    }

    @Override
    public void update(EntityManager em, Invoice invoice) {
        em.merge(invoice);
    }

    @Override
    public void delete(EntityManager em, Long id) {
        Invoice invoice = em.find(Invoice.class, id);
        if(invoice != null) {
            em.remove(invoice);
        }
    }
}
