package com.edtech.auth.Controller;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.edtech.auth.DTO.CourseRequestDto;
import com.edtech.auth.DTO.StandardApiResDTO;
import com.edtech.auth.Model.Category;
import com.edtech.auth.Model.Course;
import com.edtech.auth.Model.Users;
import com.edtech.auth.Repo.CategoryRepo;
import com.edtech.auth.Repo.CourseRepo;
import com.edtech.auth.Repo.UserRepo;
import com.edtech.auth.Services.JWTService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/v1/course")
@PreAuthorize("hasRole('INSTRUCTOR')")
public class CourseController {

    @Autowired
    JWTService jwtService;

    @Autowired
    UserRepo userRepo;

    @Autowired
    CategoryRepo categoryRepo;

    @Autowired
    CourseRepo courseRepo;

    @PostMapping("/createCourse")
    public ResponseEntity<?> createCourse(@ModelAttribute CourseRequestDto requestDto, 
    @RequestHeader("Authorization") String token) throws IOException{

        String jwtToken = token.substring(7);
        String userId = jwtService.extractId(jwtToken);
        String accountType = jwtService.extractAccountType(jwtToken);
    
        if (!accountType.equalsIgnoreCase("INSTRUCTOR")) {
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
        List<String> instructionsList = objectMapper.readValue(requestDto.getInstructions(), new TypeReference<List<String>>() {});

        
        // Handle thumbnail image upload
        String uploadDir = new File("uploads/thumbnails").getAbsolutePath();
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String filename = System.currentTimeMillis() + "_" + requestDto.getThumbnailImage().getOriginalFilename();
        File file = new File(directory, filename);

        String relativePath = "uploads/thumbnails/" + filename;

        // Save file
        requestDto.getThumbnailImage().transferTo(file);
    
        // Find instructor and category
        Users instructor = userRepo.findById(Long.valueOf(userId)).orElse(null);
        if (instructor == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Instructor not found.");
    
        Long categoryId = (categoryRepo.findByName(requestDto.getCategory())).getId();
        
        Category categoryEntity = categoryRepo.findById(categoryId).orElse(null);
        if (categoryEntity == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Category not found.");

        // Create and save Course
        Course course = new Course();
        course.setCourseName(requestDto.getCourseName());
        course.setCourseDescription(requestDto.getCourseDescription());
        course.setWhatYouWillLearn(requestDto.getWhatYouWillLearn());
        course.setPrice(requestDto.getPrice());
        course.setInstructor(instructor);
        course.setCategory(categoryEntity);
        course.setInstructions(instructionsList);
        course.setTag(tags);
        course.setThumbnail(relativePath);
        course.setStatus("Published".equalsIgnoreCase(requestDto.getStatus()) ? Course.Status.Published : Course.Status.Draft);
    
        courseRepo.save(course);
    
        instructor.getCourses().add(course);
        userRepo.save(instructor);
    
        categoryEntity.getCourse().add(course);
        categoryRepo.save(categoryEntity);
    
        return ResponseEntity.ok(new StandardApiResDTO<>(true, "Course created successfully", course));
    
    }
    
}
