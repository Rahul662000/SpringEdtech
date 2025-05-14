package com.edtech.auth.Controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.edtech.auth.DTO.CategoryDto;
import com.edtech.auth.DTO.ReviewRequestDto;
import com.edtech.auth.Model.Category;
import com.edtech.auth.Model.Course;
import com.edtech.auth.Repo.CategoryRepo;
import com.edtech.auth.Repo.CourseRepo;
import com.edtech.auth.Services.CategoryService;

import io.jsonwebtoken.security.Request;

@RestController
@RequestMapping("/api/v1/course")
public class CategoryController{

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryRepo categoryRepo;

    @Autowired
    CourseRepo courseRepo;

    @PostMapping("/createcategory")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createCategory(@RequestBody CategoryDto request) {
        return categoryService.createCategory(request);
    }

    @GetMapping("/showAllCategories")
    public ResponseEntity<?> getAllCategories() {
        try {
            List<Category> allCategories = categoryRepo.findAll();

            return ResponseEntity.ok().body(
                    new ApiResponse(true, "All categories returned successfully", allCategories)
            );

        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    new ApiResponse(false, "Something went wrong: " + e.getMessage(), null)
            );
        }
    }

    public static class ApiResponse {
        private boolean success;
        private String message;
        private Object data;

        public ApiResponse(boolean success, String message, Object data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        // Getters and setters
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Object getData() { return data; }

        public void setSuccess(boolean success) { this.success = success; }
        public void setMessage(String message) { this.message = message; }
        public void setData(Object data) { this.data = data; }
    }

    @PostMapping("/getcategorypagedetails")
    public ResponseEntity<?> getCategoryPage(@RequestBody Map<String , String> request){
        try {
            String catId = request.get("categoryId");
        Long categoryId = Long.parseLong(catId);
        System.out.println("Received categoryId: " + categoryId);

        // 1. Get selected category
        Optional<Category> selectedCategoryOpt = categoryRepo.findById(categoryId);
        if (selectedCategoryOpt.isEmpty()) {
            System.out.println("Selected category not found.");
            return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "message", "Data not found"
            ));
        }

        Category selectedCategory = selectedCategoryOpt.get();
        List<Course> selectedCourses = new ArrayList<>();
        for (Course c : selectedCategory.getCourse()) {
            if (c.getStatus() == Course.Status.Published) {
                selectedCourses.add(c);
            }
        }

        System.out.println("Selected category: " + selectedCategory.getName());
        System.out.println("Published courses in selected category: " + selectedCourses.size());

        if (selectedCourses.isEmpty()) {
            System.out.println("No published courses in selected category.");
            return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "message", "No courses found for the selected category."
            ));
        }

        // 2. Get a different category with published courses
        List<Category> otherCategories = categoryRepo.findByIdNot(categoryId);
        Category differentCategory = null;
        List<Course> differentCourses = new ArrayList<>();

        if (!otherCategories.isEmpty()) {
            Collections.shuffle(otherCategories); // Randomize selection
            for (Category cat : otherCategories) {
                List<Course> publishedCourses = new ArrayList<>();
                for (Course c : cat.getCourse()) {
                    if (c.getStatus() == Course.Status.Published) {
                        publishedCourses.add(c);
                    }
                }
                if (!publishedCourses.isEmpty()) {
                    differentCategory = cat;
                    differentCourses = publishedCourses;
                    System.out.println("Different category selected: " + differentCategory.getName());
                    System.out.println("Published courses in different category: " + differentCourses.size());
                    break;
                }
            }
        }

        if (differentCategory == null) {
            System.out.println("No different category with published courses found.");
        }

        // 3. Get top-selling published courses from all other categories
        List<Course> allPublishedCourses = new ArrayList<>();
        for (Category cat : otherCategories) {
            for (Course c : cat.getCourse()) {
                if (c.getStatus() == Course.Status.Published) {
                    allPublishedCourses.add(c);
                }
            }
        }

        System.out.println("Total published courses across other categories: " + allPublishedCourses.size());

        // Sort courses by enrolled student count (descending)
        for (int i = 0; i < allPublishedCourses.size() - 1; i++) {
            for (int j = i + 1; j < allPublishedCourses.size(); j++) {
                if (allPublishedCourses.get(i).getEnrolledStudents().size() < allPublishedCourses.get(j).getEnrolledStudents().size()) {
                    Course temp = allPublishedCourses.get(i);
                    allPublishedCourses.set(i, allPublishedCourses.get(j));
                    allPublishedCourses.set(j, temp);
                }
            }
        }

        List<Course> mostSellingCourses = new ArrayList<>();
        int limit = Math.min(10, allPublishedCourses.size());
        for (int i = 0; i < limit; i++) {
            mostSellingCourses.add(allPublishedCourses.get(i));
        }

        System.out.println("Top-selling courses fetched: " + mostSellingCourses.size());

        // Prepare response
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("selectedCategory", selectedCategory);
        responseData.put("selectedCourses", selectedCourses);
        responseData.put("differentCategory", differentCategory);
        responseData.put("differentCourses", differentCourses);
        responseData.put("mostSellingCourses", mostSellingCourses);

        return ResponseEntity.ok(Map.of("success", true, "data", responseData));
        }catch (Exception e) {
            System.out.println("Exception occurred: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Internal server error",
                    "error", e.getMessage()
            ));
        }
    }


}