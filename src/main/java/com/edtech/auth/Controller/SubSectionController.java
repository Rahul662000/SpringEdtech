package com.edtech.auth.Controller;

import static org.mockito.Mockito.description;

import java.io.File;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.edtech.auth.DTO.SectionRequestDto;
import com.edtech.auth.DTO.SubSectionDTO;
import com.edtech.auth.Model.Section;
import com.edtech.auth.Model.SubSection;
import com.edtech.auth.Repo.SectionRepo;
import com.edtech.auth.Repo.SubSectionRepo;

@RestController
@RequestMapping("/api/v1/course")
public class SubSectionController {

    @Autowired
    private SubSectionRepo subSectionRepo;

    @Autowired
    private SectionRepo sectionRepo;

    @PostMapping(value = "/addSubSection" , consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createSubSection(@ModelAttribute SubSectionDTO requestDto) {
        try {
            // Validate input
            if (requestDto.getSectionId() == null || 
                requestDto.getTitle() == null || 
                requestDto.getDescription() == null || 
                requestDto.getVideoFile() == null || 
                requestDto.getVideoFile().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "message", "All fields are required"
                ));
            }
    
            // Save video file locally
            String uploadDir = new File("uploads/videos").getAbsolutePath();
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }
    
            String filename = System.currentTimeMillis() + "_" + requestDto.getVideoFile().getOriginalFilename();
            File videoFile = new File(directory, filename);
            requestDto.getVideoFile().transferTo(videoFile);
    
            String relativePath = "videos/" + filename;

            Section section = sectionRepo.findById(requestDto.getSectionId())
                    .orElseThrow(() -> new RuntimeException("Section not found"));
    
            // Create SubSection entity (mocked for now — replace with real entity creation)
            SubSection subSection = new SubSection();
            subSection.setTitle(requestDto.getTitle());
            subSection.setDescription(requestDto.getDescription());
            subSection.setTimeDuration(requestDto.getTimeDuration());
            subSection.setVideoUrl(relativePath);
            subSection.setSection(section);
            // subSection.setSection(section);
    
            // Save subsection to DB (mocked for now)
            SubSection saved = subSectionRepo.save(subSection);

            // Add subsection to section and save
            section.getSubSection().add(saved);
            sectionRepo.save(section);
    
            return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                "success", true,
                "message", "SubSection Created Successfully",
                "data", section
                )
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Internal Server Error",
                "error", e.getMessage()
            ));
        }
    }
}
