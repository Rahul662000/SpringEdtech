package com.edtech.auth.Services;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.edtech.auth.DTO.EmailDetails;
import com.edtech.auth.Model.OTP;
import com.edtech.auth.Repo.OTPRepo;

@Service
public class OTPService {

    @Autowired
    private OTPRepo otpRepo;

    @Autowired
    private EmailService emailService;

    

    public String generateOTP(String email) {

        Optional<OTP> existing = otpRepo.findByEmail(email);
        if (existing.isPresent()) {
            return "User already registered or OTP already sent";
        }
        
        // Implement OTP generation logic (6-digit OTP, can be random)
        int otpCode = (int) (Math.random() * 900000) + 100000; // Generate 6-digit OTP

        OTP otp = new OTP();
        otp.setEmail(email);
        otp.setOtp(otpCode);
        otpRepo.save(otp);

        // Prepare email content
        Map<String, Object> variables = new HashMap<>();
        variables.put("otp", otpCode);

        EmailDetails emailDetails = new EmailDetails();
        emailDetails.setRecipient(email);
        emailDetails.setSubject("Email Verification - Edtech");
        // emailDetails.setMsgBody("Your OTP is: " + otpCode);
        emailDetails.setHtmlTemplate("OTP-template"); // must exist in templates/
        emailDetails.setVariables(variables);

        return emailService.sendSimpleMail(emailDetails);


    }
    
}
