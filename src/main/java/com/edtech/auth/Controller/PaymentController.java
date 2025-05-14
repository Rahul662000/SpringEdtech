package com.edtech.auth.Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.json.JSONObject;

import com.edtech.auth.DTO.EmailDetails;
import com.edtech.auth.Model.Course;
import com.edtech.auth.Model.CourseProgress;
import com.edtech.auth.Model.Users;
import com.edtech.auth.Repo.CourseProgressRepo;
import com.edtech.auth.Repo.CourseRepo;
import com.edtech.auth.Repo.UserRepo;
import com.edtech.auth.Services.EmailService;
import com.edtech.auth.Services.JWTService;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class PaymentController {

    @Autowired
    private CourseRepo courseRepo;
    @Autowired
    private RazorpayClient razorpayClient;
    @Autowired
    private JWTService jwtService;

    @Autowired
    UserRepo userRepo;

    @Autowired
    CourseProgressRepo courseProgressRepo;

    @Autowired
    private EmailService emailService;

    @Value("${KEY_ID_RAZORPAY}")
    private String razorpayKeyId;

    @Value("${KEY_SECRET_RAZORPAY}")
    private String secret;

    @PostMapping("/capturePayment")
    public ResponseEntity<?> capturePayment(@RequestBody Map<String, List<String>> payload , @RequestHeader("Authorization") String token){

        try {

            System.out.println(token);

            String jwtToken = token.substring(7);
            System.out.println(jwtToken);
            String uId = jwtService.extractId(jwtToken);

            Long userId = Long.parseLong(uId);

            // System.out.println(userId);

            System.out.println(payload);

            List<String> courseIds = payload.get("courses");

            System.out.println(courseIds);

            System.out.println(courseIds.getClass().getName());

            if (courseIds == null || courseIds.isEmpty()) {
                return ResponseEntity.badRequest().body("Please provide Course Ids");
            }

            double totalAmount = 0;

            
            
            for (String id : courseIds) {

                
                Long Cid = Long.parseLong(id);

                Optional<Course> optionalCourse = courseRepo.findById(Cid);
                if (optionalCourse.isEmpty()) {
                    return ResponseEntity.badRequest().body("Could not find course with ID: " + Cid);
                }

                Course course = optionalCourse.get();

                // Check if user is already enrolled
                boolean alreadyEnrolled = false;
                for (Users user : course.getEnrolledStudents()) {
                    if (user.getId().equals(userId)) {
                        alreadyEnrolled = true;
                        break;
                    }
                }

                if (alreadyEnrolled) {
                    return ResponseEntity.badRequest().body("User already enrolled in course: " + course.getCourseName());
                }

                totalAmount += course.getPrice();
            }

                // Razorpay expects amount in paise
                JSONObject options = new JSONObject();
                options.put("amount", (int) (totalAmount * 100));
                options.put("currency", "INR");
                options.put("receipt", "receipt_" + System.currentTimeMillis());

                Order order = razorpayClient.orders.create(options);

                // Convert JSONObject to Map for JSON response
                Map<String, Object> orderData = new HashMap<>();
                orderData.put("id", order.get("id"));
                orderData.put("currency", order.get("currency"));
                orderData.put("amount", order.get("amount"));

                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", orderData,
                    "key", razorpayKeyId
                ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Could not initiate order: " + e.getMessage());
        }

    }
    
    @PostMapping("/verifyPayment")
    public ResponseEntity<?> verifyPayment(@RequestBody Map<String, Object> payload, @RequestHeader("Authorization") String token){

        try{

            System.out.println("aa : " + payload);

            String razorpayOrderId = (String) payload.get("razorpay_order_id");
            String razorpayPaymentId = (String) payload.get("razorpay_payment_id");
            String razorpaySignature = (String) payload.get("razorpay_signature");
            List<String> courses = (List<String>) payload.get("courses");

            if (razorpayOrderId == null || razorpayPaymentId == null || razorpaySignature == null || courses == null || courses.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Payment failed"));
            }

            // Extract userId from token
            String jwtToken = token.substring(7); // Remove "Bearer "
            String userIdStr = jwtService.extractId(jwtToken);
            Long userId = Long.parseLong(userIdStr);

            // Create expected signature
            // String secret = System.getenv("KEY_SECRET_RAZORPAY"); // or use @Value if you prefer
            String data = razorpayOrderId + "|" + razorpayPaymentId;

            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hmacBytes = mac.doFinal(data.getBytes());
            String expectedSignature = HexFormat.of().formatHex(hmacBytes);

            System.out.println("Expected token: " + expectedSignature);
            System.out.println("Razorpay signature: " + razorpaySignature);

            if (expectedSignature.equals(razorpaySignature)) {
                // Call your service to enroll students
                enrollStudents(courses, userId);
    
                return ResponseEntity.ok(Map.of("success", true, "message", "Payment verified"));
            } else {
                return ResponseEntity.status(400).body(Map.of("success", false, "message", "Payment failed"));
            }
    

        }catch(Exception e){
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "Error verifying payment"));
 
        }

    }

    @Transactional
    public void enrollStudents(List<String> courseIds, Long userId) throws Exception {
        if (courseIds == null || courseIds.isEmpty() || userId == null) {
            throw new IllegalArgumentException("Courses or User ID is missing");
        }

        Users student = userRepo.findById(userId)
            .orElseThrow(() -> new Exception("User not found"));

        for (String courseIdStr : courseIds) {
            Long courseId = Long.parseLong(courseIdStr);

            Course course = courseRepo.findById(courseId)
                .orElseThrow(() -> new Exception("Course not found with ID: " + courseId));

            // Check if already enrolled
            if (course.getEnrolledStudents().contains(student)) {
                System.out.println("User already enrolled in course: " + course.getCourseName());
                continue;
            }

            // Enroll student
            course.getEnrolledStudents().add(student);
            courseRepo.save(course);

            System.out.println("Updated course: " + course.getCourseName());

            // Create Course Progress
            CourseProgress courseProgress = new CourseProgress();
            courseProgress.setCourse(course);
            courseProgress.setUser(student);
            courseProgress.setCompletedVideos(new ArrayList<>());
            courseProgressRepo.save(courseProgress);

            // Prepare and send the email using Thymeleaf template
            Map<String, Object> variables = new HashMap<>();
            variables.put("courseName", course.getCourseName());
            variables.put("studentName", student.getFirstName());

            EmailDetails emailDetails = new EmailDetails();
            emailDetails.setRecipient(student.getEmail());                      // to whom
            emailDetails.setSubject("Email Verification - Edtech"); // subject
            emailDetails.setHtmlTemplate("CourseEnrollment-Email");           // template name (no .html)
            emailDetails.setVariables(variables);        
                        
            emailService.sendSimpleMail(emailDetails);

            System.out.println("Email Sent Successfully to " + student.getEmail());
        }
    }

    @PostMapping("/sendPaymentSuccessfullEmail")
    public ResponseEntity<?> sendPaymentSuccessEmail(@RequestBody Map<String, Object> body , @RequestHeader("Authorization") String token) {
    
        try {
            String orderId = (String) body.get("orderId");
            String paymentId = (String) body.get("paymentId");
            Double amount = Double.parseDouble(body.get("amount").toString());

            String jwtToken = token.substring(7);
            System.out.println(jwtToken);
            String uId = jwtService.extractId(jwtToken);

            Long userId = Long.parseLong(uId);

            if (orderId == null || paymentId == null || amount == null || userId == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Please provide all the fields"));
            }

            Users student = userRepo.findById(userId)
                    .orElseThrow(() -> new Exception("User not found"));

            Map<String, Object> variables = new HashMap<>();
            variables.put("studentName", student.getFirstName());
            variables.put("amount", amount);
            variables.put("orderId", orderId);
            variables.put("paymentId", paymentId);

            EmailDetails emailDetails = new EmailDetails();
            emailDetails.setRecipient(student.getEmail());
            emailDetails.setSubject("Payment Received - StudyNotion");
            emailDetails.setHtmlTemplate("PaymentSuccess-Email"); // no .html
            emailDetails.setVariables(variables);

            emailService.sendSimpleMail(emailDetails);

            return ResponseEntity.ok(Map.of("success", true, "message", "Email sent successfully"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Could not send Email"));
        }
    
    }

}
