package com.edtech.auth.Controller;

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

import com.edtech.auth.DTO.CourseResponseDto;
import com.edtech.auth.DTO.InstructorDTO;
import com.edtech.auth.DTO.SectionDTO;
import com.edtech.auth.DTO.SectionRequestDto;
import com.edtech.auth.DTO.StandardApiResDTO;
import com.edtech.auth.DTO.SubSectionDTO;
import com.edtech.auth.Model.Course;
import com.edtech.auth.Model.Section;
import com.edtech.auth.Model.Users;
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
            sectionRepo.save(newSection);

            // Find course and add section
            Optional<Course> courseOptional = courseRepo.findById(request.getCourseId());
            if (courseOptional.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Course not found"));
            }

            Course course = courseOptional.get();
            course.getCourseContent().add(newSection);  // Assuming courseContent is a List<Section>
            Course updatedCourse = courseRepo.save(course);

            System.out.println("Enterd");


            // Populate subsection if needed — assuming JPA fetch type is configured properly
            // CourseResponseDto courseDto = mapCourseToDTO(updatedCourse);
            System.out.println("Enterd");
            return ResponseEntity.ok(Map.of("success", true, "message", "Section created successfully", "updatedCourse" , updatedCourse));
            

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Something went wrong. Unable to create section"));
        }
    }
    
//     private CourseResponseDto mapCourseToDTO(Course course) {
//     CourseResponseDto dto = new CourseResponseDto();
//     dto.setId(course.getId());
//     dto.setCourseName(course.getCourseName());
//     dto.setCourseDescription(course.getCourseDescription());
//     dto.setWhatYouWillLearn(course.getWhatYouWillLearn());
//     dto.setPrice(course.getPrice());
//     dto.setThumbnail(course.getThumbnail());
//     dto.setTag(course.getTag());
//     dto.setInstructions(course.getInstructions());
//     dto.setStatus(course.getStatus().name());
//     dto.setCreatedAt(course.getCreatedAt());

//     // Instructor mapping
//     Users instructor = course.getInstructor();
//     if (instructor != null) {
//         InstructorDTO instructorDto = new InstructorDTO();
//         instructorDto.setId(instructor.getId());
//         instructorDto.setFirstName(instructor.getFirstName());
//         instructorDto.setLastName(instructor.getLastName());
//         instructorDto.setEmail(instructor.getEmail());
//         dto.setInstructor(instructorDto);
//     }

//     // Sections and Subsections
//     List<SectionDTO> sectionDtos = course.getCourseContent().stream().map(section -> {
//         SectionDTO sectionDto = new SectionDTO();
//         sectionDto.setId(section.getId());
//         sectionDto.setSectionName(section.getSectionName());

//         List<SubSectionDTO> subSectionDtos = section.getSubSections().stream().map(subSection -> {
//             SubSectionDTO subDto = new SubSectionDTO();
//             subDto.setId(subSection.getId());
//             subDto.setTitle(subSection.getTitle());
//             subDto.setDescription(subSection.getDescription());
//             subDto.setVideoUrl(subSection.getVideoUrl());
//             return subDto;
//         }).toList();

//         sectionDto.setSubSections(subSectionDtos);
//         return sectionDto;
//     }).toList();

//     dto.setCourseContent(sectionDtos);
//     return dto;
// }

}
