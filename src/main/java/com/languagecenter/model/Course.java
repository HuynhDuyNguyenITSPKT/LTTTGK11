package com.languagecenter.model;

import com.languagecenter.model.enums.CourseLevel;
import com.languagecenter.model.enums.CourseStatus;
import com.languagecenter.model.enums.DurationUnit;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id")
    private Long id;

    @Column(name = "course_name", nullable = false, length = 200)
    private String courseName;

    @Lob
    private String description;

    @Enumerated(EnumType.STRING)
    private CourseLevel level;

    private Integer duration;

    @Enumerated(EnumType.STRING)
    @Column(name = "duration_unit")
    private DurationUnit durationUnit;

    @Column(nullable = false)
    private Double fee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourseStatus status;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Course() {
    }

    public Course( String courseName, CourseLevel level, String description, Integer duration, DurationUnit durationUnit, Double fee, CourseStatus status) {
        this.courseName = courseName;
        this.level = level;
        this.description = description;
        this.duration = duration;
        this.durationUnit = durationUnit;
        this.fee = fee;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public CourseLevel getLevel() {
        return level;
    }

    public void setLevel(CourseLevel level) {
        this.level = level;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public DurationUnit getDurationUnit() {
        return durationUnit;
    }

    public void setDurationUnit(DurationUnit durationUnit) {
        this.durationUnit = durationUnit;
    }

    public Double getFee() {
        return fee;
    }

    public void setFee(Double fee) {
        this.fee = fee;
    }

    public CourseStatus getStatus() {
        return status;
    }

    public void setStatus(CourseStatus status) {
        this.status = status;
    }
}