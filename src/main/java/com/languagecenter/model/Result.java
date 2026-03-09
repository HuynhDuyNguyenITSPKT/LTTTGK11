package com.languagecenter.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "results",
        uniqueConstraints = @UniqueConstraint(name = "uq_results", columnNames = {"student_id", "class_id"}))
public class Result {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "result_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private Class classEntity;

    @Column(precision = 5, scale = 2)
    private BigDecimal score;

    @Column(length = 10)
    private String grade;

    @Column(length = 255)
    private String comment;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Result() {}

    public Result(Student student, Class classEntity, BigDecimal score, String grade, String comment) {
        this.student = student;
        this.classEntity = classEntity;
        this.score = score;
        this.grade = grade;
        this.comment = comment;
    }

    // ─── Getters ───────────────────────────────────────────────────────────────

    public Long getId() { return id; }
    public Student getStudent() { return student; }
    public Class getClassEntity() { return classEntity; }
    public BigDecimal getScore() { return score; }
    public String getGrade() { return grade; }
    public String getComment() { return comment; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // ─── Setters ───────────────────────────────────────────────────────────────

    public void setId(Long id) { this.id = id; }
    public void setStudent(Student student) { this.student = student; }
    public void setClassEntity(Class classEntity) { this.classEntity = classEntity; }
    public void setScore(BigDecimal score) { this.score = score; }
    public void setGrade(String grade) { this.grade = grade; }
    public void setComment(String comment) { this.comment = comment; }

    @Override
    public String toString() {
        return (student != null ? student.getFullName() : "?")
                + " - " + (classEntity != null ? classEntity.getClassName() : "?");
    }
}
