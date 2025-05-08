package com.edtech.auth.Model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class CourseProgress {

     @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "course_id")
    @JsonIgnoreProperties({"courseContent", "ratingAndReview", "instructor", "category", "enrolledStudents", "instructions"})
    private Course course;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"courses", "courseProgress", "userProfile"})
    private Users user;

    @ManyToMany
    private List<SubSection> completedVideos;
    
}
