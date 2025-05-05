package com.edtech.auth.Repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.edtech.auth.Model.Course;

public interface CourseRepo extends JpaRepository<Course , Long>{
    
}
