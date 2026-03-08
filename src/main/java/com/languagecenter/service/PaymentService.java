package com.languagecenter.service;

import com.languagecenter.db.TransactionManager;
import com.languagecenter.model.Invoice;
import com.languagecenter.model.Payment;
import com.languagecenter.model.enums.InvoiceStatus;
import com.languagecenter.model.enums.PaymentStatus;
import com.languagecenter.repo.InvoiceRepository;
import com.languagecenter.repo.PaymentRepository;

import java.util.List;

public class PaymentService {

    private final PaymentRepository paymentRepo;
    private final InvoiceRepository invoiceRepo;
    private final TransactionManager tx;

    public PaymentService(PaymentRepository paymentRepo,
                         InvoiceRepository invoiceRepo,
                         TransactionManager tx) {
        this.paymentRepo = paymentRepo;
        this.invoiceRepo = invoiceRepo;
        this.tx = tx;
    }

    public List<Payment> getAll() throws Exception {
        return tx.runInTransaction(paymentRepo::findAll);
    }

    public Payment getById(Long id) throws Exception {
        return tx.runInTransaction(em -> paymentRepo.findById(em, id));
    }

    /**
     * Lấy số tiền còn thiếu cho một invoice
     */
    public Double getRemainingAmount(Long invoiceId) throws Exception {
        return tx.runInTransaction(em -> {
            Invoice invoice = invoiceRepo.findById(em, invoiceId);
            if (invoice == null) return 0.0;

            Double totalPaid = paymentRepo.getTotalPaidByInvoiceId(em, invoiceId);
            Double remaining = invoice.getTotalAmount() - totalPaid;

            return remaining > 0 ? remaining : 0.0;
        });
    }

    /**
     * Tạo payment mới và tự động kiểm tra xem Invoice đã được thanh toán đầy đủ chưa.
     * Nếu tổng tiền thanh toán >= tổng tiền hóa đơn, cập nhật Invoice status = Paid
     * CHẶN nếu thanh toán dư
     */
    public void create(Payment payment) throws Exception {
        tx.runInTransaction(em -> {

            // Đảm bảo enrollment được set từ invoice
            if (payment.getInvoice() != null && payment.getEnrollment() == null) {
                payment.setEnrollment(payment.getInvoice().getEnrollment());
            }

            // Đảm bảo student được set từ invoice
            if (payment.getInvoice() != null && payment.getStudent() == null) {
                payment.setStudent(payment.getInvoice().getStudent());
            }

            // Validate: payment phải có invoice
            if (payment.getInvoice() == null) {
                throw new Exception("Payment phải có Invoice!");
            }

            Long invoiceId = payment.getInvoice().getId();

            // Lấy invoice
            Invoice invoice = invoiceRepo.findById(em, invoiceId);

            if (invoice == null) {
                throw new Exception("Invoice không tồn tại!");
            }

            // Tính tổng tiền đã thanh toán (chỉ tính payment status = Completed)
            Double totalPaid = paymentRepo.getTotalPaidByInvoiceId(em, invoiceId);

            // Kiểm tra nếu thanh toán dư
            Double newTotal = totalPaid + payment.getAmount();
            if (newTotal > invoice.getTotalAmount()) {
                Double remaining = invoice.getTotalAmount() - totalPaid;
                throw new Exception(
                    String.format("Thanh toán dư!\n" +
                        "Tổng hóa đơn: %.0f VNĐ\n" +
                        "Đã thanh toán: %.0f VNĐ\n" +
                        "Còn thiếu: %.0f VNĐ\n" +
                        "Số tiền thanh toán không được vượt quá số tiền còn thiếu!",
                        invoice.getTotalAmount(), totalPaid, remaining)
                );
            }

            // Insert payment
            paymentRepo.create(em, payment);

            // Cập nhật lại totalPaid sau khi insert
            totalPaid = paymentRepo.getTotalPaidByInvoiceId(em, invoiceId);

            // Kiểm tra nếu đã thanh toán đủ
            if (totalPaid >= invoice.getTotalAmount()
                && invoice.getStatus() != InvoiceStatus.Paid) {

                // Cập nhật invoice status = Paid
                invoice.setStatus(InvoiceStatus.Paid);
                invoiceRepo.update(em, invoice);
            }

            return null;
        });
    }

    public void update(Payment payment) throws Exception {
        tx.runInTransaction(em -> {
            Payment existingPayment = paymentRepo.findById(em, payment.getId());
            if(existingPayment.getStatus() == PaymentStatus.Completed || payment.getStatus() == PaymentStatus.Failed) {
                throw new Exception("Không thể cập nhật payment đã hoàn thành!");
            }

            // Đảm bảo enrollment được set từ invoice
            if (payment.getInvoice() != null && payment.getEnrollment() == null) {
                payment.setEnrollment(payment.getInvoice().getEnrollment());
            }

            // Đảm bảo student được set từ invoice
            if (payment.getInvoice() != null && payment.getStudent() == null) {
                payment.setStudent(payment.getInvoice().getStudent());
            }

            // Validate payment phải có invoice
            if (payment.getInvoice() == null) {
                throw new Exception("Payment phải có Invoice!");
            }

            Long invoiceId = payment.getInvoice().getId();
            Invoice invoice = invoiceRepo.findById(em, invoiceId);

            if (invoice == null) {
                throw new Exception("Invoice không tồn tại!");
            }

            // Lấy payment cũ để trừ amount cũ
            Payment oldPayment = paymentRepo.findById(em, payment.getId());
            Double oldAmount = (oldPayment != null) ? oldPayment.getAmount() : 0.0;

            // Tính tổng tiền đã thanh toán (không bao gồm payment hiện tại)
            Double totalPaid = paymentRepo.getTotalPaidByInvoiceId(em, invoiceId);
            totalPaid = totalPaid - oldAmount; // Trừ đi amount cũ

            // Kiểm tra nếu thanh toán dư
            Double newTotal = totalPaid + payment.getAmount();
            if (newTotal > invoice.getTotalAmount()) {
                Double remaining = invoice.getTotalAmount() - totalPaid;
                throw new Exception(
                    String.format("Thanh toán dư!\n" +
                        "Tổng hóa đơn: %.0f VNĐ\n" +
                        "Đã thanh toán: %.0f VNĐ\n" +
                        "Còn thiếu: %.0f VNĐ\n" +
                        "Số tiền thanh toán không được vượt quá số tiền còn thiếu!",
                        invoice.getTotalAmount(), totalPaid, remaining)
                );
            }

            // Update payment
            paymentRepo.update(em, payment);

            // Kiểm tra lại invoice status
            totalPaid = paymentRepo.getTotalPaidByInvoiceId(em, invoiceId);

            // Nếu đã thanh toán đủ → Paid
            if (totalPaid >= invoice.getTotalAmount()) {
                if (invoice.getStatus() != InvoiceStatus.Paid) {
                    invoice.setStatus(InvoiceStatus.Paid);
                    invoiceRepo.update(em, invoice);
                }
            }
            // Nếu chưa thanh toán đủ và hiện tại là Paid → chuyển về Issued
            else if (invoice.getStatus() == InvoiceStatus.Paid) {
                invoice.setStatus(InvoiceStatus.Issued);
                invoiceRepo.update(em, invoice);
            }

            return null;
        });
    }

    public void delete(Long id) throws Exception {
        tx.runInTransaction(em -> {

            Payment payment = paymentRepo.findById(em, id);

            if (payment != null) {
                Long invoiceId = payment.getInvoice() != null ?
                    payment.getInvoice().getId() : null;

                // Xóa payment
                paymentRepo.delete(em, id);

                // Kiểm tra lại invoice status
                if (invoiceId != null) {
                    Invoice invoice = invoiceRepo.findById(em, invoiceId);

                    if (invoice != null) {
                        Double totalPaid = paymentRepo.getTotalPaidByInvoiceId(em, invoiceId);

                        // Nếu sau khi xóa mà chưa đủ tiền, chuyển về Issued
                        if (totalPaid < invoice.getTotalAmount()
                            && invoice.getStatus() == InvoiceStatus.Paid) {

                            invoice.setStatus(InvoiceStatus.Issued);
                            invoiceRepo.update(em, invoice);
                        }
                    }
                }
            }

            return null;
        });
    }
}
