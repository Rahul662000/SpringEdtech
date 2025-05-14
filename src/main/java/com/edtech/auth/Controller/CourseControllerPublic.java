package com.edtech.auth.Controller;

import java.util.HashMap;
import java.util.List;
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

import com.edtech.auth.DTO.FullCourseDetailsResponse;
import com.edtech.auth.Model.AdditionalDetails;
import com.edtech.auth.Model.Course;
import com.edtech.auth.Model.CourseProgress;
import com.edtech.auth.Model.Section;
import com.edtech.auth.Model.SubSection;
import com.edtech.auth.Model.Users;
import com.edtech.auth.Repo.AdditionalDetailsRepo;
import com.edtech.auth.Repo.CourseProgressRepo;
import com.edtech.auth.Repo.CourseRepo;
import com.edtech.auth.Repo.SubSectionRepo;
import com.edtech.auth.Repo.UserRepo;
import com.edtech.auth.Services.JWTService;

@RestController
@RequestMapping("/api/v1/course")
public class CourseControllerPublic {

    @Autowired
    CourseRepo courseRepo;

    @Autowired
    AdditionalDetailsRepo additionalDetailsRepo;

    @Autowired
    SubSectionRepo subSectionRepo;

    @Autowired
    JWTService jwtService;

    @Autowired
    CourseProgressRepo courseProgressRepo;

    @Autowired
    UserRepo userRepo;
    
    @PostMapping("/getCourseDetails")
    public ResponseEntity<?> getCourseDetails(@RequestBody Map<String, String> request){

        try {
            String courseId = request.get("courseId");
            if (courseId == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Course ID is required"));
            }

            Optional<Course> courseOpt = courseRepo.findById(Long.parseLong(courseId));
            if (courseOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "Could not find the course with ID: " + courseId));
            }

            Course courseDetails = courseOpt.get();

            // Manually fetch nested instructor additionalDetails (simulate populate)
            Users instructor = courseDetails.getInstructor();
            if (instructor != null && instructor.getUserProfile() != null) {
                Long addId = instructor.getUserProfile().getId();
                Optional<AdditionalDetails> addDetails = additionalDetailsRepo.findById(addId);
                if (addDetails.isPresent()) {
                    instructor.setUserProfile(addDetails.get());
                }
            }

            int totalDurationInSeconds = 0;
            List<Section> sectionList = courseDetails.getCourseContent();
            for (int i = 0; i < sectionList.size(); i++) {
                Section section = sectionList.get(i);

                List<SubSection> subSections = subSectionRepo.findBySectionId(section.getId());
                section.setSubSection(subSections);

                for (int j = 0; j < subSections.size(); j++) {
                        SubSection sub = subSections.get(j);
                        try {
                            int duration = Integer.parseInt(sub.getTimeDuration());
                            totalDurationInSeconds += duration;
                        } catch (NumberFormatException e) {
                            // Skip invalid durations
                        }
                }
            }

            String totalDuration = convertSecondsToDuration(totalDurationInSeconds);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("courseDetails", courseDetails);
            responseData.put("totalDuration", totalDuration);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Course details fetched successfully",
                    "data", responseData
            ));


        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }

    }

    public String convertSecondsToDuration(int totalSeconds) {
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
    
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

        @PostMapping("/getFullCourseDetails")
    public ResponseEntity<?> getFullCourseDetails(@RequestBody Map<String, String> request , @RequestHeader("Authorization") String token) {
    try {
            String jwtToken = token.substring(7);
            String userId = jwtService.extractId(jwtToken);
            String accountType = jwtService.extractAccountType(jwtToken);
        
        // if (!accountType.equalsIgnoreCase("INSTRUCTOR")) {
        //     return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only instructors can access Instructor Courses.");
        // }

        Long instructorId = Long.parseLong(userId);
        Long courseId = Long.parseLong(request.get("courseId"));
        // String userEmail = principal.getName(); // assumes Spring Security with email-based login

        // Fetch user
        Users user = userRepo.findById(instructorId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Fetch course with all necessary joins (use custom JPQL or projections if needed)
        Course courseDetails = courseRepo.findCourseWithDetailsById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Get Course Progress
        Optional<CourseProgress> progressOpt = courseProgressRepo.findByCourseIdAndUserId(courseId, user.getId());

         // Calculate total duration without using streams
        int totalDurationInSeconds = 0;
        for (Section section : courseDetails.getCourseContent()) {
            for (SubSection subSection : section.getSubSection()) {
                try {
                    totalDurationInSeconds += Integer.parseInt(subSection.getTimeDuration());
                } catch (NumberFormatException e) {
                    // You can log or handle the invalid duration format here if needed
                }
            }
        }

        String formattedDuration = convertSecondsToDuration1(totalDurationInSeconds);

        // Response DTO
        FullCourseDetailsResponse response = new FullCourseDetailsResponse(
            courseDetails,
            formattedDuration,
            progressOpt.map(CourseProgress::getCompletedVideos).orElse(List.of())
        );

        return ResponseEntity.ok().body(Map.of("success", true, "data", response));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", e.getMessage()));
        }

    }

    private String convertSecondsToDuration1(int totalSeconds) {
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        StringBuilder duration = new StringBuilder();
        if (hours > 0) duration.append(hours).append("h ");
        if (minutes > 0 || hours > 0) duration.append(minutes).append("m ");
        duration.append(seconds).append("s");

        return duration.toString().trim();
    }


}
