package com.edtech.auth.Services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.edtech.auth.DTO.CategoryDto;
import com.edtech.auth.Model.Category;
import com.edtech.auth.Repo.CategoryRepo;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepo categoryRepo;

    public ResponseEntity<?> createCategory(CategoryDto request) {
        // Validation
        if (request.getName() == null || request.getDescription() == null ||
            request.getName().isBlank() || request.getDescription().isBlank()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "All fields are required");
            return ResponseEntity.badRequest().body(response);
        }

        // Save to DB
        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        categoryRepo.save(category);

        // Return response
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Category created successfully");
        return ResponseEntity.ok(response);
    }
    
}
