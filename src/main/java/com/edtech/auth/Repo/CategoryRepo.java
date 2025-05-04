package com.edtech.auth.Repo;


import org.springframework.data.jpa.repository.JpaRepository;

import com.edtech.auth.Model.Category;

public interface CategoryRepo extends JpaRepository<Category , Long>{

    Object findByCategory(String category);
    
}
