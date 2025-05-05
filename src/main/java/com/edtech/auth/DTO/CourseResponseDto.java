package com.edtech.auth.DTO;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class CourseResponseDto {
    private Long id;
    private String courseName;
    private String courseDescription;
    private String whatYouWillLearn;
    private Double price;
    private String thumbnail;
    private List<String> tag;
    private List<String> instructions;
    private String status;
    private Date createdAt;
    private InstructorDTO instructor;
    private List<SectionDTO> courseContent;
}
