package com.languagecenter;

import com.languagecenter.db.Jpa;
import jakarta.persistence.EntityManager;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        try {
            EntityManager em = Jpa.em();
//            Object result = em.createNativeQuery("SELECT COUNT(*) FROM students")
//                    .getSingleResult();
//
//            System.out.println("Total students: " + result.toString());





            em.close();
            Jpa.shutdown();
        } catch (Exception e) {
            System.out.println("❌ Connection failed!");
            e.printStackTrace();
        }

    }
}