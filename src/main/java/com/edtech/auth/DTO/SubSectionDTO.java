package com.edtech.auth.DTO;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class SubSectionDTO {
    private Long sectionId;
    private String title;
    private String description;
    private String timeDuration;
    private MultipartFile videoFile;
}
