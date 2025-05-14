package com.edtech.auth.DTO;

import com.edtech.auth.Model.Course;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseDTO {
    
    private Course course;
    private String totalDuration;
    private double progressPercentage;

}
