package com.edtech.auth.Controller;

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.edtech.auth.DTO.CourseRequestDto;
import com.edtech.auth.Model.Course;
import com.edtech.auth.Model.Users;
import com.edtech.auth.Repo.CategoryRepo;
import com.edtech.auth.Repo.CourseRepo;
import com.edtech.auth.Repo.UserRepo;
import com.edtech.auth.Services.CourseService;
import com.edtech.auth.Services.JWTService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/v1/Course")
@PreAuthorize("hasRole('INSTRUCTOR')")
public class CourseController {

    @Autowired
    CourseService courseService;

    @Autowired
    JWTService jwtService;

    @Autowired
    UserRepo userRepo;

    @Autowired
    CategoryRepo categoryRepo;

    @Autowired
    CourseRepo courseRepo;



    public ResponseEntity<?> createCourse(@ModelAttribute CourseRequestDto requestDto,  @RequestHeader("Authorization") String token) throws IOException{

        String jwtToken = token.substring(7);
        String userId = jwtService.extractId(jwtToken);
        String accountType = jwtService.extractAccountType(jwtToken);

        if (!accountType.equalsIgnoreCase("Instructor")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only instructors can create courses.");
        }

        // Validate required fields
        if (requestDto.getCourseName() == null || requestDto.getCourseDescription() == null ||
            requestDto.getWhatYouWillLearn() == null || requestDto.getPrice() == null ||
            requestDto.getCategory() == null || requestDto.getInstructions() == null ||
            requestDto.getTag() == null || requestDto.getThumbnailImage() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("All fields are mandatory.");
        }

        // Parse tag and instructions
        ObjectMapper objectMapper = new ObjectMapper();
        List<String> tags = objectMapper.readValue(requestDto.getTag(), new TypeReference<List<String>>() {});
        List<String> instructions = objectMapper.readValue(requestDto.getInstructions(), new TypeReference<List<String>>() {});

        // Save image locally
        MultipartFile thumbnail = requestDto.getThumbnailImage();
        String fileName = System.currentTimeMillis() + "_" + thumbnail.getOriginalFilename();
        String uploadDir = "uploads/thumbnails/";
        File uploadPath = new File(uploadDir);
        if (!uploadPath.exists()) uploadPath.mkdirs();
        File file = new File(uploadDir + fileName);
        thumbnail.transferTo(file);

        // Find instructor and category
        Users instructor = userRepo.findById(Long.valueOf(userId)).orElse(null);
        if (instructor == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Instructor not found.");

        Category category = categoryRepo.findById(Long.valueOf(requestDto.getCategory())).orElse(null);
        if (category == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Category not found.");

        // Create Course
        Course course = new Course();
        course.setCourseName(requestDto.getCourseName());
        course.setCourseDescription(requestDto.getCourseDescription());
        course.setWhatYouWillLearn(requestDto.getWhatYouWillLearn());
        course.setPrice(requestDto.getPrice());
        course.setInstructor(instructor);
        course.setCategory(category);
        course.setInstructions(instructions);
        course.setTag(tags);
        course.setThumbnail("/" + uploadDir + fileName);  // store as relative path
        course.setStatus(
            requestDto.getStatus() != null && requestDto.getStatus().equalsIgnoreCase("Published") 
            ? Course.Status.Published 
            : Course.Status.Draft
        );

        courseRepo.save(course);

        // update instructor
        instructor.getCourse().add(course);
        userRepo.save(instructor);

        // update category
        category.getCourse().add(course);
        categoryRepo.save(category);

        return ResponseEntity.ok(course);

    }
    
}
