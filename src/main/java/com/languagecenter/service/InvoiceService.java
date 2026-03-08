package com.languagecenter.service;

import com.languagecenter.db.TransactionManager;
import com.languagecenter.model.Invoice;
import com.languagecenter.repo.InvoiceRepository;

import java.util.List;

public class InvoiceService {

    private final InvoiceRepository repo;
    private final TransactionManager tx;

    public InvoiceService(InvoiceRepository repo, TransactionManager tx) {
        this.repo = repo;
        this.tx = tx;
    }

    public List<Invoice> getAll() throws Exception {
        return tx.runInTransaction(repo::findAll);
    }

    public Invoice getById(Long id) throws Exception {
        return tx.runInTransaction(em -> repo.findById(em, id));
    }

    public Invoice getByEnrollmentId(Long enrollmentId) throws Exception {
        return tx.runInTransaction(em -> repo.findByEnrollmentId(em, enrollmentId));
    }

    public List<Invoice> getByStudentId(Long studentId) throws Exception {
        return tx.runInTransaction(em -> repo.findByStudentId(em, studentId));
    }

    public void create(Invoice invoice) throws Exception {
        tx.runInTransaction(em -> {
            repo.create(em, invoice);
            return null;
        });
    }

    public void update(Invoice invoice) throws Exception {
        tx.runInTransaction(em -> {
            repo.update(em, invoice);
            return null;
        });
    }

    public void delete(Long id) throws Exception {
        tx.runInTransaction(em -> {
            repo.delete(em, id);
            return null;
        });
    }
}
