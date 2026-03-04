package com.languagecenter;

import com.languagecenter.db.Jpa;
import com.languagecenter.db.TransactionManager;
import com.languagecenter.model.Student;
import com.languagecenter.repo.StudentRepository;
import com.languagecenter.repo.jpa.JpaStudentRepository;
import com.languagecenter.service.StudentService;
import com.languagecenter.stream.StudentStreamQueries;
import jakarta.persistence.EntityManager;

import java.util.List;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        try {
            StudentRepository repo = new JpaStudentRepository();
            TransactionManager tx = new TransactionManager();
            StudentService studentService = new StudentService(repo, tx);
            List<Student> students = studentService.getAll();
            for (Student student : students) {
                System.out.println(student.getFullName());
            }
            System.out.println("------------------------------");
            String key = "L ";
            List<Student> st = StudentStreamQueries.searchByName(students,key);
            for (Student student : st) {
                System.out.println(student.getFullName());
            }
        } catch (Exception e) {
            System.out.println("❌ Connection failed!");
            e.printStackTrace();
        }

    }
}