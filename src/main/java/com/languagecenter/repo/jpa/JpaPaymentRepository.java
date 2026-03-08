package com.languagecenter.repo.jpa;

import com.languagecenter.model.Payment;
import com.languagecenter.model.enums.PaymentStatus;
import com.languagecenter.repo.PaymentRepository;
import jakarta.persistence.EntityManager;

import java.util.List;

public class JpaPaymentRepository implements PaymentRepository {

    @Override
    public List<Payment> findAll(EntityManager em) {
        return em.createQuery(
                """
                select p from Payment p
                left join fetch p.student
                left join fetch p.invoice
                order by p.paymentDate desc
                """,
                Payment.class
        ).getResultList();
    }

    @Override
    public Payment findById(EntityManager em, Long id) {
        return em.find(Payment.class, id);
    }

    @Override
    public Double getTotalPaidByInvoiceId(EntityManager em, Long invoiceId) {
        Double total = em.createQuery(
                """
                select sum(p.amount)
                from Payment p
                where p.invoice.id = :invoiceId
                and p.status = :status
                """,
                Double.class
        )
        .setParameter("invoiceId", invoiceId)
        .setParameter("status", PaymentStatus.Completed)
        .getSingleResult();

        return total != null ? total : 0.0;
    }

    @Override
    public void create(EntityManager em, Payment payment) {
        em.persist(payment);
    }

    @Override
    public void update(EntityManager em, Payment payment) {
        em.merge(payment);
    }

    @Override
    public void delete(EntityManager em, Long id) {
        Payment payment = em.find(Payment.class, id);
        if(payment != null) em.remove(payment);
    }
}
