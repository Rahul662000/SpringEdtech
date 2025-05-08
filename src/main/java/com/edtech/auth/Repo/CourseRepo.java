package com.edtech.auth.Repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.edtech.auth.Model.Course;
import com.edtech.auth.Model.Users;

public interface CourseRepo extends JpaRepository<Course , Long>{

    List<Course> findByInstructorIdOrderByCreatedAtDesc(Long instructorId);

    Optional<Course> findCourseWithDetailsById(Long courseId);

    List<Course> findByInstructorId(Long instructorId);
    
}
