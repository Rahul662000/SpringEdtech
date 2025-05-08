package com.edtech.auth.Controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.edtech.auth.DTO.SectionRequestDto;
import com.edtech.auth.Model.Course;
import com.edtech.auth.Model.Section;
import com.edtech.auth.Repo.CourseRepo;
import com.edtech.auth.Repo.SectionRepo;
import com.edtech.auth.Services.JWTService;

@RestController
@RequestMapping("/api/v1/course")
public class SectionController {

    @Autowired
    private SectionRepo sectionRepo;

    @Autowired
    private CourseRepo courseRepo;

    @Autowired
    JWTService jwtService;

    @PostMapping("/addSection")
    public ResponseEntity<?> createSection(@RequestBody SectionRequestDto request , @RequestHeader("Authorization") String token) {

        String jwtToken = token.substring(7);
        String userId = jwtService.extractId(jwtToken);
        String accountType = jwtService.extractAccountType(jwtToken);
    
        if (!accountType.equalsIgnoreCase("INSTRUCTOR")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only instructors can create courses.");
        }

        try {
            // Data validation
            if (request.getSectionName() == null || request.getCourseId() == null) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of("success", false, "message", "All fields are mandatory. Check once."));
            }

            // Create new section
            Section newSection = new Section();
            newSection.setSectionName(request.getSectionName());
            

            // Find course and add section
            Optional<Course> courseOptional = courseRepo.findById(request.getCourseId());
            if (courseOptional.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Course not found"));
            }

            Course course = courseOptional.get();
            course.getCourseContent().add(newSection);  // Assuming courseContent is a List<Section>
            newSection.setCourse(course);
            

            System.out.println("Enterd");


            // Populate subsection if needed — assuming JPA fetch type is configured properly
            // CourseResponseDto courseDto = mapCourseToDTO(updatedCourse);
            System.out.println("Enterd");
            sectionRepo.save(newSection);
            Course updatedCourse = courseRepo.save(course);
            return ResponseEntity.ok(Map.of("success", true, "message", "Section created successfully", "updatedCourse" , updatedCourse));
            

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Something went wrong. Unable to create section"));
        }
    }


}
