package com.edtech.auth.Repo;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.edtech.auth.Model.Category;

public interface CategoryRepo extends JpaRepository<Category , Long>{

    Category findByName(String category);

    // Optional findById(Long category);
    
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.course co LEFT JOIN FETCH co.ratingAndReview WHERE c.id = :id AND co.status = 'Published'")
    Optional<Category> findByIdWithPublishedCourses(@Param("id") Long id);

    List<Category> findByIdNot(Long id);
}
