package com.edtech.auth.Controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.edtech.auth.Controller.CategoryController.ApiResponse;
import com.edtech.auth.DTO.DisplayPictureRequestDto;
import com.edtech.auth.DTO.InstructorCourseDto;
import com.edtech.auth.DTO.StandardApiResDTO;
import com.edtech.auth.DTO.UpdateProfileDto;
import com.edtech.auth.Model.AdditionalDetails;
import com.edtech.auth.Model.Course;
import com.edtech.auth.Model.Users;
import com.edtech.auth.Repo.AdditionalDetailsRepo;
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

}
