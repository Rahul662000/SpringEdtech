package com.edtech.auth.Model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "otp")
public class OTP{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private int otp;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt = new Date();


}





// package com.edtech.auth.Services;

// import com.edtech.auth.DTO.EmailDetails;
// import com.edtech.auth.Entities.Otp;
// import com.edtech.auth.Repository.OtpRepository;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;
// import java.util.HashMap;
// import java.util.Map;
// import java.util.Optional;
// import java.util.Random;

// @Service
// public class OtpService {

//     @Autowired
//     private OtpRepository otpRepository;

//     @Autowired
//     private EmailService emailService;

//     public String generateAndSendOtp(String email) {
//         // Check if OTP already exists for the user
//         Optional<Otp> existing = otpRepository.findByEmail(email);
//         if (existing.isPresent()) {
//             return "User already registered or OTP already sent";
//         }

//         // Generate a 6-digit numeric OTP
//         String otpCode = String.valueOf(100000 + new Random().nextInt(900000));

//         // Save OTP
//         Otp otp = new Otp();
//         otp.setEmail(email);
//         otp.setOtp(otpCode);
//         otpRepository.save(otp);

//         // Prepare email content
//         Map<String, Object> variables = new HashMap<>();
//         variables.put("otp", otpCode);

//         EmailDetails emailDetails = new EmailDetails();
//         emailDetails.setRecipient(email);
//         emailDetails.setSubject("Email Verification - StudyNotion");
//         emailDetails.setMsgBody("Your OTP is: " + otpCode);
//         emailDetails.setHtmlTemplate("otp-verification-template"); // must exist in templates/
//         emailDetails.setVariables(variables);

//         // Send email
//         return emailService.sendSimpleMail(emailDetails);
//     }
// }

// package com.edtech.auth.Controllers;

// import com.edtech.auth.Services.OtpService;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// @RestController
// @RequestMapping("/api/auth")
// public class OtpController {

//     @Autowired
//     private OtpService otpService;

//     @PostMapping("/send-otp")
//     public ResponseEntity<?> sendOtp(@RequestParam String email) {
//         String result = otpService.generateAndSendOtp(email);

//         if (result.contains("already")) {
//             return ResponseEntity.status(409).body(result);
//         }

//         return ResponseEntity.ok(result);
//     }
// }