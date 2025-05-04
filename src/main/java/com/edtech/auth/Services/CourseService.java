package com.edtech.auth.Services;

import java.security.Principal;

import com.edtech.auth.DTO.CourseRequestDto;
import com.edtech.auth.Model.Course;
import com.fasterxml.jackson.databind.JsonMappingException;

public class CourseService {


    public Course createCourse(CourseRequestDto dto, Principal principal) throws JsonMappingException{

        return new Course();

    }


    }
    
