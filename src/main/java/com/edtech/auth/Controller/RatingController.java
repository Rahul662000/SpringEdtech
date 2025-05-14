package com.edtech.auth.Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.edtech.auth.DTO.ReviewRequestDto;
import com.edtech.auth.Model.Course;
import com.edtech.auth.Model.RatingAndReview;
import com.edtech.auth.Model.Users;
import com.edtech.auth.Repo.CourseRepo;
import com.edtech.auth.Repo.RatingAndReviewRepo;
import com.edtech.auth.Repo.UserRepo;
import com.edtech.auth.Services.JWTService;

@RestController
@RequestMapping("/api/v1/course")
public class RatingController {

    @Autowired
    JWTService jwtService;

    @Autowired
    UserRepo userRepo;

    @Autowired
    CourseRepo courseRepo;

    @Autowired
    RatingAndReviewRepo ratingAndReviewRepo;

    @PostMapping("/createRating")
    public ResponseEntity<?> createRating(@RequestBody ReviewRequestDto request, @RequestHeader("Authorization") String token){

        try{

            String jwtToken = token.substring(7);
            Long userId = Long.parseLong(jwtService.extractId(jwtToken));

            // Get user
            Optional<Users> optionalUser = userRepo.findById(userId);
            if (!optionalUser.isPresent()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
            }
            Users user = optionalUser.get();

            // Get course
            Optional<Course> optionalCourse = courseRepo.findById(Long.parseLong(request.getCourseId()));
            if (!optionalCourse.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Course not found");
            }
            Course course = optionalCourse.get();

            // Check if user is enrolled
            if (!course.getEnrolledStudents().contains(user)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Student is not enrolled in the course");
            }

            // Check if already reviewed
            Optional<RatingAndReview> existingReview = ratingAndReviewRepo.findByUserAndCourse(user, course);
            if (existingReview.isPresent()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Course already reviewed by the user");
            }

            // Create review
            RatingAndReview review = new RatingAndReview();
            review.setUser(user);
            review.setCourse(course);
            review.setRating(request.getRating());
            review.setReview(request.getReview());
            ratingAndReviewRepo.save(review);

            // Update course (optional if bidirectional)
            course.getRatingAndReview().add(review);
            courseRepo.save(course);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Rating And Review Successfully Created",
                    "data", review
            ));

        }catch(Exception e){

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));

        }

    }

    @GetMapping("/getReviews")
    public ResponseEntity<Map<String, Object>> getAllRatings() {

        List<RatingAndReview> allRatings = ratingAndReviewRepo.findAllByOrderByRatingDesc();

        List<Map<String, Object>> dataList = new ArrayList<>();

        for (RatingAndReview rating : allRatings) {
            Map<String, Object> data = new HashMap<>();
            data.put("id", rating.getId());
            data.put("rating", rating.getRating());
            data.put("review", rating.getReview());

            Users user = rating.getUser();
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("firstName", user.getFirstName());
            userData.put("lastName", user.getLastName());
            userData.put("email", user.getEmail());
            userData.put("image", user.getImage());

            Course course = rating.getCourse();
            Map<String, Object> courseData = new HashMap<>();
            courseData.put("id", course.getId());
            courseData.put("courseName", course.getCourseName());

            data.put("user", userData);
            data.put("course", courseData);

            dataList.add(data);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", dataList);

        return ResponseEntity.ok(response);

    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAverageRating(@RequestBody Map<String, Object> request) {
    Map<String, Object> response = new HashMap<>();

        try {
            Long courseId = Long.valueOf(request.get("courseId").toString());

            Double averageRating = ratingAndReviewRepo.getAverageRatingByCourseId(courseId);

            if (averageRating != null) {
                response.put("success", true);
                response.put("averageRating", averageRating);
            } else {
                response.put("success", true);
                response.put("message", "Average rating is 0, no rating given");
                response.put("averageRating", 0.0);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}
