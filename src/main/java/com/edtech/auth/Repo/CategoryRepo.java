package com.edtech.auth.Repo;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.edtech.auth.Model.Category;

public interface CategoryRepo extends JpaRepository<Category , Long>{

    Category findByName(String category);

    // Optional findById(Long category);
    
}
