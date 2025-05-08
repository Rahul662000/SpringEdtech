package com.edtech.auth.DTO;

import java.util.List;

import com.edtech.auth.Model.Course;
import com.edtech.auth.Model.SubSection;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FullCourseDetailsResponse {
    
    private Course courseDetails;
    private String totalDuration;
    private List<SubSection> completedVideos;


}
