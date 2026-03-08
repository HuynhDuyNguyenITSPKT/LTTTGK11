package com.languagecenter.repo;

import com.languagecenter.model.Payment;
import jakarta.persistence.EntityManager;

import java.util.List;

public interface PaymentRepository {

    List<Payment> findAll(EntityManager em);

    Payment findById(EntityManager em, Long id);

    Double getTotalPaidByInvoiceId(EntityManager em, Long invoiceId);

    void create(EntityManager em, Payment payment);

    void update(EntityManager em, Payment payment);

    void delete(EntityManager em, Long id);
}
