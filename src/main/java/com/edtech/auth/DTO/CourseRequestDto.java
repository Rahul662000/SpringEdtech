package com.edtech.auth.DTO;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class CourseRequestDto {

    private String courseId;
    private String courseName;
    private String courseDescription;
    private String whatYouWillLearn;
    private Double price;
    private String category;          // categoryId
    private String status;
    private String instructions;      // JSON string
    private String tag;               // JSON string
    private MultipartFile thumbnailImage;
    private Long InstructorId;
    
}
