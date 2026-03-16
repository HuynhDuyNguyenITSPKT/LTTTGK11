package com.languagecenter.stream;

import com.languagecenter.model.Payment;
import com.languagecenter.model.enums.PaymentMethod;
import com.languagecenter.model.enums.PaymentStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class PaymentStreamQueries {

    /**
     * Lọc payment theo tên học sinh
     */
    public static List<Payment> filterByStudentName(List<Payment> data, String keyword) {
        if (keyword == null || keyword.isBlank())
            return data;

        return data.stream()
                .filter(p -> p.getStudent() != null &&
                        p.getStudent().getFullName()
                                .toLowerCase()
                                .contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * Lọc payment theo Invoice ID
     */
    public static List<Payment> filterByInvoiceId(List<Payment> data, Long invoiceId) {
        if (invoiceId == null)
            return data;

        return data.stream()
                .filter(p -> p.getInvoice() != null &&
                        p.getInvoice().getId().equals(invoiceId))
                .collect(Collectors.toList());
    }

    /**
     * Lọc payment theo trạng thái
     */
    public static List<Payment> filterByStatus(List<Payment> data, PaymentStatus status) {
        if (status == null)
            return data;

        return data.stream()
                .filter(p -> p.getStatus() == status)
                .collect(Collectors.toList());
    }

    /**
     * Lọc payment theo phương thức thanh toán
     */
    public static List<Payment> filterByPaymentMethod(List<Payment> data, PaymentMethod method) {
        if (method == null)
            return data;

        return data.stream()
                .filter(p -> p.getPaymentMethod() == method)
                .collect(Collectors.toList());
    }

    /**
     * Lọc payment theo khoảng thời gian thanh toán
     */
    public static List<Payment> filterByDateRange(List<Payment> data,
                                                  LocalDateTime fromDate,
                                                  LocalDateTime toDate) {
        if (fromDate == null && toDate == null)
            return data;

        return data.stream()
                .filter(p -> {
                    LocalDateTime paymentDate = p.getPaymentDate();

                    if (fromDate != null && paymentDate.isBefore(fromDate))
                        return false;

                    if (toDate != null && paymentDate.isAfter(toDate))
                        return false;

                    return true;
                })
                .collect(Collectors.toList());
    }

    /**
     * Lọc payment theo khoảng số tiền
     */
    public static List<Payment> filterByAmountRange(List<Payment> data,
                                                    Double minAmount,
                                                    Double maxAmount) {
        if (minAmount == null && maxAmount == null)
            return data;

        return data.stream()
                .filter(p -> {
                    Double amount = p.getAmount();

                    if (minAmount != null && amount < minAmount)
                        return false;

                    if (maxAmount != null && amount > maxAmount)
                        return false;

                    return true;
                })
                .collect(Collectors.toList());
    }

    /**
     * Lọc payment theo mã tham chiếu
     */
    public static List<Payment> filterByReferenceCode(List<Payment> data, String referenceCode) {
        if (referenceCode == null || referenceCode.isBlank())
            return data;

        return data.stream()
                .filter(p -> p.getReferenceCode() != null &&
                        p.getReferenceCode().toLowerCase()
                                .contains(referenceCode.toLowerCase()))
                .collect(Collectors.toList());
    }
}
