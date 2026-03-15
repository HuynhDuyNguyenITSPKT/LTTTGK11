package com.languagecenter.service;

import com.languagecenter.db.TransactionManager;
import com.languagecenter.model.Invoice;
import com.languagecenter.model.Payment;
import com.languagecenter.model.enums.InvoiceStatus;
import com.languagecenter.model.enums.PaymentStatus;
import com.languagecenter.repo.InvoiceRepository;
import com.languagecenter.repo.PaymentRepository;

import java.util.List;

/**
 * Xử lý nghiệp vụ thanh toán (Payment).
 * <p>
 * Class chỉ chịu trách nhiệm quản lý thanh toán, tự động cập nhật
 * các thông tin liên đới trong Hóa đơn một cách chặt chẽ.
 * </p>
 */
public class PaymentService {

    private final PaymentRepository paymentRepo;
    private final InvoiceRepository invoiceRepo;
    private final TransactionManager tx;

    /**
     * Khởi tạo service bằng Dependency Injection cho repo và transaction manager.
     *
     */
    public PaymentService(PaymentRepository paymentRepo,
                         InvoiceRepository invoiceRepo,
                         TransactionManager tx) {
        this.paymentRepo = paymentRepo;
        this.invoiceRepo = invoiceRepo;
        this.tx = tx;
    }

    /**
     * Lấy toàn bộ giao dịch thanh toán hiện có.
     *
     */
    public List<Payment> getAll() throws Exception {
        return tx.runInTransaction(paymentRepo::findAll);
    }

    /**
     * Lấy giao dịch theo ID.
     *
     */
    public Payment getById(Long id) throws Exception {
        return tx.runInTransaction(em -> paymentRepo.findById(em, id));
    }

    /**
     * Tính số tiền thanh toán còn thiếu từ một hóa đơn.
     *
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
     * Thực hiện thanh toán mới và thay đổi tự động trạng thái hóa đơn.
     * Nếu tiền thừa, hệ thống sẽ thực hiện ngăn chặn thao tác.
     *
     */
    public void create(Payment payment) throws Exception {
        tx.runInTransaction(em -> {
            if (payment.getInvoice() != null && payment.getEnrollment() == null) {
                payment.setEnrollment(payment.getInvoice().getEnrollment());
            }

            if (payment.getInvoice() != null && payment.getStudent() == null) {
                payment.setStudent(payment.getInvoice().getStudent());
            }

            if (payment.getInvoice() == null) {
                throw new Exception("Payment phải có Invoice!");
            }

            Long invoiceId = payment.getInvoice().getId();
            Invoice invoice = invoiceRepo.findById(em, invoiceId);

            if (invoice == null) {
                throw new Exception("Invoice không tồn tại!");
            }

            Double totalPaid = paymentRepo.getTotalPaidByInvoiceId(em, invoiceId);
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

            paymentRepo.create(em, payment);

            totalPaid = paymentRepo.getTotalPaidByInvoiceId(em, invoiceId);

            if (totalPaid >= invoice.getTotalAmount()
                && invoice.getStatus() != InvoiceStatus.Paid) {
                invoice.setStatus(InvoiceStatus.Paid);
                invoiceRepo.update(em, invoice);
            }

            return null;
        });
    }

    /**
     * Sửa chi tiết trên biên lai thanh toán và tính toán lại công nợ vào hóa đơn.
     *
     */
    public void update(Payment payment) throws Exception {
        tx.runInTransaction(em -> {
            Payment existingPayment = paymentRepo.findById(em, payment.getId());
            if(existingPayment.getStatus() == PaymentStatus.Completed || payment.getStatus() == PaymentStatus.Failed) {
                throw new Exception("Không thể cập nhật payment đã hoàn thành!");
            }

            if (payment.getInvoice() != null && payment.getEnrollment() == null) {
                payment.setEnrollment(payment.getInvoice().getEnrollment());
            }

            if (payment.getInvoice() != null && payment.getStudent() == null) {
                payment.setStudent(payment.getInvoice().getStudent());
            }

            if (payment.getInvoice() == null) {
                throw new Exception("Payment phải có Invoice!");
            }

            Long invoiceId = payment.getInvoice().getId();
            Invoice invoice = invoiceRepo.findById(em, invoiceId);

            if (invoice == null) {
                throw new Exception("Invoice không tồn tại!");
            }

            Payment oldPayment = paymentRepo.findById(em, payment.getId());
            Double oldAmount = (oldPayment != null) ? oldPayment.getAmount() : 0.0;

            Double totalPaid = paymentRepo.getTotalPaidByInvoiceId(em, invoiceId);
            totalPaid = totalPaid - oldAmount;

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

            paymentRepo.update(em, payment);

            totalPaid = paymentRepo.getTotalPaidByInvoiceId(em, invoiceId);

            if (totalPaid >= invoice.getTotalAmount()) {
                if (invoice.getStatus() != InvoiceStatus.Paid) {
                    invoice.setStatus(InvoiceStatus.Paid);
                    invoiceRepo.update(em, invoice);
                }
            } else if (invoice.getStatus() == InvoiceStatus.Paid) {
                invoice.setStatus(InvoiceStatus.Issued);
                invoiceRepo.update(em, invoice);
            }

            return null;
        });
    }

    /**
     * Thu hồi lại thanh toán và đổi thông tin hóa đơn nếu tiền lùi về.
     *
     */
    public void delete(Long id) throws Exception {
        tx.runInTransaction(em -> {

            Payment payment = paymentRepo.findById(em, id);

            if (payment != null) {
                Long invoiceId = payment.getInvoice() != null ? payment.getInvoice().getId() : null;

                paymentRepo.delete(em, id);

                if (invoiceId != null) {
                    Invoice invoice = invoiceRepo.findById(em, invoiceId);

                    if (invoice != null) {
                        Double totalPaid = paymentRepo.getTotalPaidByInvoiceId(em, invoiceId);

                        if (totalPaid < invoice.getTotalAmount() && invoice.getStatus() == InvoiceStatus.Paid) {
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
