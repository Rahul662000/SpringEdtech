package com.edtech.auth.Repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.edtech.auth.Model.Course;
import com.edtech.auth.Model.Course.Status;
import com.edtech.auth.Model.Users;

public interface CourseRepo extends JpaRepository<Course , Long>{

    List<Course> findByInstructorIdOrderByCreatedAtDesc(Long instructorId);

    Optional<Course> findCourseWithDetailsById(Long courseId);

    List<Course> findByInstructorId(Long instructorId);

    List<Course> findByStatus(Status published);

    List<Course> findByEnrolledStudentsContaining(Users user);
    
}
