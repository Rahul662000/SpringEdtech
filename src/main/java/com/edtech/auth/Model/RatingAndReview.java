package com.edtech.auth.Model;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class RatingAndReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    // @JsonBackRe/ference
    private Users user;

    @Column(nullable = false)
    private int rating;

    @Column(nullable = false)
    private String review;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    // @JsonBackReference
    private Course course;

}
