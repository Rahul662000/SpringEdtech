package com.edtech.auth.Repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.edtech.auth.Model.Course;
import com.edtech.auth.Model.CourseProgress;
import com.edtech.auth.Model.Users;

public interface CourseProgressRepo extends JpaRepository<CourseProgress , Long>{

    Optional<CourseProgress> findByCourseIdAndUserId(Long courseId, Long id);
    
    void deleteByUserId(Long userId);

    Optional<CourseProgress> findByCourseAndUser(Course course, Users user);

}
