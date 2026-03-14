package com.languagecenter.service;

import com.languagecenter.db.TransactionManager;
import com.languagecenter.model.UserAccount;
import com.languagecenter.model.enums.StudentStatus;
import com.languagecenter.model.enums.TeacherStatus;
import com.languagecenter.repo.UserAccountRepository;
import com.languagecenter.util.PasswordUtil;

/**
 * Xử lý xác thực người dùng và đăng nhập vào hệ thống.
 * <p>
 * Class này tuân thủ Single Responsibility Principle (SRP) bằng cách chỉ đảm nhận
 * việc xác minh thông tin đăng nhập và duy trì lớp bảo mật cơ bản.
 * </p>
 */
public class AuthService {

    private final UserAccountRepository repo;
    private final TransactionManager tx;

    /**
     * Khởi tạo service bảo mật với repository.
     *
     * @param repo Repository tài khoản truy vấn
     * @param tx   Công cụ quản lý giao dịch DB
     */
    public AuthService(UserAccountRepository repo, TransactionManager tx) {
        this.repo = repo;
        this.tx = tx;
    }

    /**
     * Thực hiện kiểm tra thông tin đăng nhập từ input.
     *
     * @param username Tên đăng nhập
     * @param password Mật khẩu thô (sẽ được kiểm tra với mã băm)
     * @return Thông tin tài khoản người dùng nếu thành công
     * @throws Exception Các lỗi về bảo mật như tài khoản bị khó, thông tin sai, v.v.
     */
    public UserAccount login(String username, String password) throws Exception {

        return tx.runInTransaction(em -> {
            UserAccount user = repo.findByUsername(em, username);

            if (user == null) {
                throw new RuntimeException("Invalid username or password");
            }

            if (!Boolean.TRUE.equals(user.getIsActive())) {
                throw new RuntimeException("Account disabled");
            }

            if (user.getStudent() != null && user.getStudent().getStatus() != StudentStatus.Active) {
                throw new RuntimeException("Student account is not active");
            }

            if (user.getTeacher() != null && user.getTeacher().getStatus() != TeacherStatus.Active) {
                throw new RuntimeException("Teacher account is not active");
            }

            boolean match = PasswordUtil.verify(password, user.getPasswordHash());

            if (!match) {
                throw new RuntimeException("Invalid username or password");
            }

            return user;
        });
    }
}