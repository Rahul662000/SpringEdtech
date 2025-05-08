package com.edtech.auth.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InstructorCourseDto {
    
    private Long id;
    private String courseName;
    private String courseDescription;
    private int totalStudentsEnrolled;
    private double totalAmountGenerated;

}
