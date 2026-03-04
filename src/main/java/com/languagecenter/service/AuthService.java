package com.languagecenter.service;

import com.languagecenter.db.TransactionManager;
import com.languagecenter.model.UserAccount;
import com.languagecenter.repo.UserAccountRepository;
import com.languagecenter.util.PasswordUtil;

public class AuthService {

    private final UserAccountRepository repo;
    private final TransactionManager tx;

    public AuthService(UserAccountRepository repo,TransactionManager tx){
        this.repo = repo;
        this.tx = tx;
    }

    public UserAccount login(String username,String password) throws Exception{

        return tx.runInTransaction(em -> {
                UserAccount user =
                        repo.findByUsername(em, username);

                if (user == null) {
                    throw new RuntimeException("Invalid username or password");
                }

                if (!Boolean.TRUE.equals(user.getIsActive())) {
                    throw new RuntimeException("Account disabled");
                }

                boolean match =
                        PasswordUtil.verify(password, user.getPasswordHash());

                if (!match) {
                    throw new RuntimeException("Invalid username or password");
                }

                return user;
            });
    }
}