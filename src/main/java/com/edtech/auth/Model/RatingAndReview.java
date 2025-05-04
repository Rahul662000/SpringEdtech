package com.edtech.auth.Model;

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
    private Users user;

    @Column(nullable = false)
    private int rating;

    @Column(nullable = false)
    private String review;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

}
