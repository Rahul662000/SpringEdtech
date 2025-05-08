package com.edtech.auth.Repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.edtech.auth.Model.CourseProgress;

public interface CourseProgressRepo extends JpaRepository<CourseProgress , Long>{

    Optional<CourseProgress> findByCourseIdAndUserId(Long courseId, Long id);
    


}
