package com.languagecenter.init;

import com.languagecenter.db.TransactionManager;
import com.languagecenter.model.UserAccount;
import com.languagecenter.model.enums.UserRole;
import com.languagecenter.repo.UserAccountRepository;
import com.languagecenter.util.PasswordUtil;

public class AppInitializer {

    public static void initAdmin(TransactionManager tx,
                                 UserAccountRepository repo) throws Exception {

        tx.runInTransaction(em -> {

            UserAccount admin =
                    repo.findByUsername(em,"admin");

            if(admin == null){

                UserAccount acc =
                        new UserAccount(
                                "admin",
                                PasswordUtil.hash("admin"),
                                UserRole.Admin,
                                true
                        );

                repo.save(em,acc);

                System.out.println("Default admin created");
                System.out.println("username: admin");
                System.out.println("password: admin");
            }

            return null;
        });
    }
}