package com.languagecenter.stream;

import com.languagecenter.model.Invoice;
import com.languagecenter.model.enums.InvoiceStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class InvoiceStreamQueries {

    /**
     * Lọc invoice theo tên học sinh
     */
    public static List<Invoice> filterByStudentName(List<Invoice> data, String keyword) {
        if (keyword == null || keyword.isBlank())
            return data;

        return data.stream()
                .filter(i -> i.getStudent() != null &&
                        i.getStudent().getFullName()
                                .toLowerCase()
                                .contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * Lọc invoice theo tên lớp học
     */
    public static List<Invoice> filterByClassName(List<Invoice> data, String keyword) {
        if (keyword == null || keyword.isBlank())
            return data;

        return data.stream()
                .filter(i -> i.getEnrollment() != null &&
                        i.getEnrollment().getClassEntity() != null &&
                        i.getEnrollment().getClassEntity().getClassName()
                                .toLowerCase()
                                .contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * Lọc invoice theo trạng thái
     */
    public static List<Invoice> filterByStatus(List<Invoice> data, InvoiceStatus status) {
        if (status == null)
            return data;

        return data.stream()
                .filter(i -> i.getStatus() == status)
                .collect(Collectors.toList());
    }

    /**
     * Lọc invoice theo khoảng thời gian phát hành
     */
    public static List<Invoice> filterByDateRange(List<Invoice> data,
                                                  LocalDate fromDate,
                                                  LocalDate toDate) {
        if (fromDate == null && toDate == null)
            return data;

        return data.stream()
                .filter(i -> {
                    LocalDate issueDate = i.getIssueDate();

                    if (fromDate != null && issueDate.isBefore(fromDate))
                        return false;

                    if (toDate != null && issueDate.isAfter(toDate))
                        return false;

                    return true;
                })
                .collect(Collectors.toList());
    }

    /**
     * Lọc invoice theo khoảng số tiền
     */
    public static List<Invoice> filterByAmountRange(List<Invoice> data,
                                                    Double minAmount,
                                                    Double maxAmount) {
        if (minAmount == null && maxAmount == null)
            return data;

        return data.stream()
                .filter(i -> {
                    Double amount = i.getTotalAmount();

                    if (minAmount != null && amount < minAmount)
                        return false;

                    if (maxAmount != null && amount > maxAmount)
                        return false;

                    return true;
                })
                .collect(Collectors.toList());
    }

    /**
     * Lấy tổng số tiền của các invoice
     */
    public static Double getTotalAmount(List<Invoice> data) {
        return data.stream()
                .mapToDouble(Invoice::getTotalAmount)
                .sum();
    }

    /**
     * Lấy các invoice chưa thanh toán
     */
    public static List<Invoice> getUnpaidInvoices(List<Invoice> data) {
        return data.stream()
                .filter(i -> i.getStatus() == InvoiceStatus.Issued ||
                            i.getStatus() == InvoiceStatus.Draft)
                .collect(Collectors.toList());
    }

    /**
     * Lấy các invoice đã thanh toán
     */
    public static List<Invoice> getPaidInvoices(List<Invoice> data) {
        return data.stream()
                .filter(i -> i.getStatus() == InvoiceStatus.Paid)
                .collect(Collectors.toList());
    }
}
