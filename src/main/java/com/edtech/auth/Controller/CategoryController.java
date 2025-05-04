package com.edtech.auth.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.edtech.auth.DTO.CategoryDto;
import com.edtech.auth.Model.Category;
import com.edtech.auth.Repo.CategoryRepo;
import com.edtech.auth.Services.CategoryService;

@RestController
@RequestMapping("/api/v1/Course")
public class CategoryController{

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryRepo categoryRepo;

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

}