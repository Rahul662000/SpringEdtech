package com.edtech.auth.Controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.edtech.auth.DTO.CourseDTO;
import com.edtech.auth.DTO.DisplayPictureRequestDto;
import com.edtech.auth.DTO.InstructorCourseDto;
import com.edtech.auth.DTO.StandardApiResDTO;
import com.edtech.auth.DTO.UpdateProfileDto;
import com.edtech.auth.Model.AdditionalDetails;
import com.edtech.auth.Model.Course;
import com.edtech.auth.Model.CourseProgress;
import com.edtech.auth.Model.Section;
import com.edtech.auth.Model.SubSection;
import com.edtech.auth.Model.Users;
import com.edtech.auth.Repo.AdditionalDetailsRepo;
import com.edtech.auth.Repo.CourseProgressRepo;
import com.edtech.auth.Repo.CourseRepo;
import com.edtech.auth.Repo.UserRepo;
import com.edtech.auth.Services.JWTService;

@RestController
@RequestMapping("/api/v1/profile")
public class ProfileController {
    
    @Autowired
    private CourseRepo courseRepo;

    @Autowired
    JWTService jwtService;

    @Autowired
    UserRepo userRepo;

    @Autowired
    AdditionalDetailsRepo profileRepo;

    @Autowired
    CourseProgressRepo courseProgressRepo;

    @GetMapping("/instructor")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<?> getInstructorDashboard(@RequestHeader("Authorization") String token) {
        try {

            String jwtToken = token.substring(7);
            Long userId = Long.parseLong(jwtService.extractId(jwtToken));

            System.out.println("User Id is : " + userId);

            List<Course> courseList = courseRepo.findByInstructorId(userId);

            List<InstructorCourseDto> courseData = new ArrayList<>();

        for (Course course : courseList) {
            int totalStudents = course.getEnrolledStudents() != null ? course.getEnrolledStudents().size() : 0;
            double totalRevenue = totalStudents * (course.getPrice() != null ? course.getPrice() : 0.0);

            InstructorCourseDto dto = new InstructorCourseDto(
                course.getId(),
                course.getCourseName(),
                course.getCourseDescription(),
                totalStudents,
                totalRevenue
            );

            courseData.add(dto);
        }

            return ResponseEntity.ok().body(Map.of("course", courseData));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal Server Error");
        }
    }

    @PutMapping("/updateDisplayPicture")
    public ResponseEntity<?> updateDisplayPicture( @ModelAttribute DisplayPictureRequestDto request, @RequestHeader("Authorization") String token) {

        try {
                String jwtToken = token.substring(7);
                String userId = jwtService.extractId(jwtToken);

                MultipartFile file = request.getDisplayPicture();

                if (file.isEmpty()) {
                    return ResponseEntity.badRequest().body(Map.of("success", false, "message", "No file uploaded"));
                }

                // Create directory if it doesn't exist
                String uploadDir = new File("uploads/profilePicture").getAbsolutePath();
                File directory = new File(uploadDir);
                if (!directory.exists()) {
                    directory.mkdirs();
                }

                String filename = System.currentTimeMillis() + "_" + request.getDisplayPicture().getOriginalFilename();
                File file1 = new File(directory, filename);

                String relativePath = "profilePicture/" + filename;

                // Save file
                request.getDisplayPicture().transferTo(file1);

                // Save file path to user's profile (example)
                Users user = userRepo.findById(Long.parseLong(userId))
                        .orElseThrow(() -> new RuntimeException("User not found"));
                user.setImage("/" + relativePath.toString().replace("\\", "/"));
                userRepo.save(user);

                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Image uploaded successfully",
                    "data", user
                ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("success", false, "message", e.getMessage())
            );
        }

    }

    @PutMapping("/updateProfile")
    public ResponseEntity<?> updateProfile(@RequestBody UpdateProfileDto request, @RequestHeader("Authorization") String token) {

        try {

            String jwtToken = token.substring(7);
            String userId = jwtService.extractId(jwtToken); // Decode user ID from JWT

            // Basic validation
            if (request.getContactNumber() == null || request.getContactNumber().isEmpty()
                    || request.getGender() == null || request.getGender().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "All fields are mandatory"
                ));
            }

            // Fetch user and profile
            Users user = userRepo.findById(Long.parseLong(userId))
                    .orElseThrow(() -> new RuntimeException("User not found"));

            AdditionalDetails profile = profileRepo.findByUser_Id(user.getId())
                    .orElseThrow(() -> new RuntimeException("Profile not found"));

            // Update user
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            userRepo.save(user);

            // Update profile
            profile.setDateOfBirth(request.getDateOfBirth());
            profile.setAbout(request.getAbout());
            profile.setContactNumber(request.getContactNumber());
            profile.setGender(request.getGender());
            profileRepo.save(profile);

            // Refresh updated user with populated profile
            Users updatedUser = userRepo.findById(user.getId()).get();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Profile updated successfully",
                    "updatedUserDetails", updatedUser
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Unable to update profile",
                    "error", e.getMessage()
            ));
        }

    }

    @GetMapping("/getuserdetails")
    public ResponseEntity<?> getUserAllDetails(@RequestParam Long userId) {
        try {
            Users userDetails = userRepo.findById(userId).orElse(null);

            if (userDetails == null) {
                return new ResponseEntity<>(new StandardApiResDTO<>(false, "User not found"), HttpStatus.NOT_FOUND);
            }

            return new ResponseEntity<>(new StandardApiResDTO<>(true, "User Data Fetched Successfully", userDetails), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new StandardApiResDTO<>(false, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/deleteProfile")
    public ResponseEntity<?> deleteUsers(@RequestHeader("Authorization") String token){

        try{

            String jwtToken = token.substring(7);
            String userId = jwtService.extractId(jwtToken);

            Long id = Long.parseLong(userId);

            // Find user by ID
            Optional<Users> optionalUser = userRepo.findById(id);
            if (optionalUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "User Not Found"
                ));
            }

            Users user = optionalUser.get();

            // 1. Remove user from enrolled courses
            List<Course> allCourses = courseRepo.findAll();
            for (Course course : allCourses) {
                if (course.getEnrolledStudents().removeIf(enrolled -> enrolled.getId().equals(id))) {
                    courseRepo.save(course);
                }
            }

            // 2. Delete course progress
            courseProgressRepo.deleteByUserId(id);

            // 3. Delete profile (AdditionalDetails)
            if (user.getUserProfile() != null) {
                profileRepo.deleteById(user.getUserProfile().getId());
            }

            // Finally, delete the user
            userRepo.deleteById(id);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Account Deleted Successfully"
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Something went wrong, Unable to Delete profile"
            ));
        }

    }

    @GetMapping("/getEnrolledCourses")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> getEnrolledCourses(@RequestHeader("Authorization") String token){

        try{

            String jwtToken = token.substring(7);
            String uId = jwtService.extractId(jwtToken);

            Long userId = Long.parseLong(uId);

            Optional<Users> optionalUser = userRepo.findById(userId);

            if (optionalUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "User not found"));
            }
            Users user = optionalUser.get();

            List<CourseDTO> courses = new ArrayList<>();

            for (Course course : user.getEnrolledCourses()) {
                int totalDurationInSeconds = 0;
                int totalSubSections = 0;
        
                if (course.getCourseContent() != null) {
                    for (Section section : course.getCourseContent()) {
                        if (section.getSubSection() != null) {
                            for (SubSection sub : section.getSubSection()) {
                                totalDurationInSeconds += safeParse(sub.getTimeDuration());
                                totalSubSections++;
                            }
                        }
                    }
                }
        
                // Convert seconds to duration string
                String totalDurationStr = convertSecondsToDuration(totalDurationInSeconds);
        
                // Fetch course progress
                Optional<CourseProgress> progressOpt = courseProgressRepo.findByCourseAndUser(course, user);
                int completedVideos = progressOpt.map(p -> p.getCompletedVideos().size()).orElse(0);
        
                double progress = (totalSubSections == 0) ? 100.0
                        : Math.round((double) completedVideos / totalSubSections * 10000.0) / 100.0;
        
                courses.add(new CourseDTO(course, totalDurationStr, progress));
            }
            return ResponseEntity.ok(Map.of("success", true, "data", courses));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }

    }

    public String convertSecondsToDuration(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;
        return String.format("%02dh:%02dm:%02ds", hours, minutes, secs);
    }

    private int safeParse(String time) {
        try {
            return Integer.parseInt(time);
        } catch (NumberFormatException e) {
            return 0;
        }
    }


}
