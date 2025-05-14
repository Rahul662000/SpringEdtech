package com.edtech.auth.Repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.edtech.auth.Model.Course;
import com.edtech.auth.Model.RatingAndReview;
import com.edtech.auth.Model.Users;

@Repository
public interface RatingAndReviewRepo extends JpaRepository<RatingAndReview, Long>{

    Optional<RatingAndReview> findByUserAndCourse(Users user, Course course);

    List<RatingAndReview> findAllByOrderByRatingDesc();

    @Query("SELECT AVG(r.rating) FROM RatingAndReview r WHERE r.course.id = :courseId")
    Double getAverageRatingByCourseId(@Param("courseId") Long courseId);

}
