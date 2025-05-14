package com.edtech.auth.Controller;

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.edtech.auth.DTO.CourseRequestDto;
import com.edtech.auth.DTO.FullCourseDetailsResponse;
import com.edtech.auth.DTO.StandardApiResDTO;
import com.edtech.auth.Model.Category;
import com.edtech.auth.Model.Course;
import com.edtech.auth.Model.CourseProgress;
import com.edtech.auth.Model.Section;
import com.edtech.auth.Model.SubSection;
import com.edtech.auth.Model.Users;
import com.edtech.auth.Repo.CategoryRepo;
import com.edtech.auth.Repo.CourseProgressRepo;
import com.edtech.auth.Repo.CourseRepo;
import com.edtech.auth.Repo.SectionRepo;
import com.edtech.auth.Repo.UserRepo;
import com.edtech.auth.Services.JWTService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.edtech.auth.Repo.SubSectionRepo;


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

    @Autowired
    CourseProgressRepo courseProgressRepo;

    @Autowired
    SubSectionRepo subSectionRepo;

    @Autowired
    SectionRepo sectionRepo;

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

        String relativePath = "thumbnails/" + filename;

        // Save file
        requestDto.getThumbnailImage().transferTo(file);
    
        // Find instructor and category
        Users instructor = userRepo.findById(Long.valueOf(userId)).orElse(null);
        if (instructor == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Instructor not found.");
    
            Long categoryId = Long.parseLong(requestDto.getCategory());

        // Long categoryId = (categoryRepo.findByName(requestDto.getCategory())).getId();
        
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
    
    @PostMapping(value = "/editCourse" , consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> editCourse(@ModelAttribute CourseRequestDto request)
    {
        try {
            
            Long courseId = Long.parseLong(request.getCourseId());

            Optional<Course> optionalCourse = courseRepo.findById(courseId);
            if (optionalCourse.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "message", "Course not found"));
            }

            Course course = optionalCourse.get();

            MultipartFile thumbnailImage = request.getThumbnailImage();
            if (thumbnailImage != null && !thumbnailImage.isEmpty()) {
                String uploadDir = new File("uploads/thumbnails").getAbsolutePath();
                File directory = new File(uploadDir);
                if (!directory.exists()) directory.mkdirs();

                String filename = System.currentTimeMillis() + "_" + thumbnailImage.getOriginalFilename();
                File imageFile = new File(directory, filename);
                thumbnailImage.transferTo(imageFile);
                course.setThumbnail("thumbnails/" + filename);

            }
            // ✅ Update fields if not null
            if (request.getCourseName() != null) course.setCourseName(request.getCourseName());
            if (request.getCourseDescription() != null) course.setCourseDescription(request.getCourseDescription());
            if (request.getWhatYouWillLearn() != null) course.setWhatYouWillLearn(request.getWhatYouWillLearn());
            if (request.getPrice() != null) course.setPrice(request.getPrice());
            if (request.getStatus() != null) course.setStatus("Published".equalsIgnoreCase(request.getStatus()) ? Course.Status.Published : Course.Status.Draft);

            // ✅ Handle JSON string fields (tags and instructions)
            ObjectMapper mapper = new ObjectMapper();
            if (request.getTag() != null) {
                List<String> tags = mapper.readValue(request.getTag(), new TypeReference<>() {});
                course.setTag(tags);
            }
            if (request.getInstructions() != null) {
                List<String> instructions = mapper.readValue(request.getInstructions(), new TypeReference<>() {});
                course.setInstructions(instructions);
            }


            // Save updated course
            Course updatedCourse = courseRepo.save(course);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Course updated successfully",
                "data", updatedCourse
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Internal server error",
                "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/getInstructorCourses")
    public ResponseEntity<?> getInstructorCourses(@RequestHeader("Authorization") String token) {

        try {
            String jwtToken = token.substring(7);
            String userId = jwtService.extractId(jwtToken);
            String accountType = jwtService.extractAccountType(jwtToken);
        
            if (!accountType.equalsIgnoreCase("INSTRUCTOR")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only instructors can access Instructor Courses.");
            }

            Long instructorId = Long.parseLong(userId);

            List<Course> instructorCourses = courseRepo
                    .findByInstructorIdOrderByCreatedAtDesc(instructorId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", instructorCourses
            ));   
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Failed to retrieve instructor courses",
                    "error", e.getMessage()
            ));
        }

    }

    @DeleteMapping("/deleteCourse")
    public ResponseEntity<?> deleteCourse(@RequestBody Map<String,String> request , @RequestHeader("Authorization") String token){

        try{

            String jwtToken = token.substring(7);
            String userId = jwtService.extractId(jwtToken);
            String accountType = jwtService.extractAccountType(jwtToken);
        
            if (!accountType.equalsIgnoreCase("INSTRUCTOR")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only instructors can access Instructor Courses.");
            }

            Long instructorId = Long.parseLong(userId);
            Long courseId = Long.parseLong(request.get("courseId"));
            Optional<Course> optionalCourse = courseRepo.findById(courseId);

            Course course = optionalCourse.get();
            
            // Clear enrolled students
            course.getEnrolledStudents().clear();

            // Delete subsections and sections
            for (Section section : course.getCourseContent()) {
                for (SubSection subSection : section.getSubSection()) {
                    subSectionRepo.delete(subSection);
                }
                sectionRepo.delete(section);
            }


            // Delete course
            courseRepo.delete(course);

            return ResponseEntity.ok(Map.of("success", true, "message", "Course deleted successfully"));

        }
        catch(Exception e){

            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Server error",
                "error", e.getMessage()
            ));

        }

    }

}
