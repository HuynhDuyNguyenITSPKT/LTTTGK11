package com.languagecenter.repo;

import com.languagecenter.model.Invoice;
import jakarta.persistence.EntityManager;

import java.util.List;

public interface InvoiceRepository {

    List<Invoice> findAll(EntityManager em);

    Invoice findById(EntityManager em, Long id);

    Invoice findByEnrollmentId(EntityManager em, Long enrollmentId);

    List<Invoice> findByStudentId(EntityManager em, Long studentId);

    void create(EntityManager em, Invoice invoice);

    void update(EntityManager em, Invoice invoice);

    void delete(EntityManager em, Long id);
}
