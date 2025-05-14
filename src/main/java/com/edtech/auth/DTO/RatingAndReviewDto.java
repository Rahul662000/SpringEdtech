package com.edtech.auth.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RatingAndReviewDto {
    private Long id;
    private int rating;
    private String review;

    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private String image;

    private Long courseId;
    private String courseName;
}
