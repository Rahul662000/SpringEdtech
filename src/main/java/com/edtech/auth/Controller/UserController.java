package com.edtech.auth.Controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.edtech.auth.DTO.AuthResponse;
import com.edtech.auth.DTO.EmailRequest;
import com.edtech.auth.DTO.OTPresponseDTO;
import com.edtech.auth.DTO.SignUpReponseDto;
import com.edtech.auth.DTO.SignUpRequestDto;
import com.edtech.auth.Model.Users;
import com.edtech.auth.Services.OTPService;
import com.edtech.auth.Services.UserServices;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("api/v1/auth")
// @CrossOrigin(origins="http://localhost:3000")
public class UserController {
    
    @Autowired
    private UserServices userService;

    @Autowired
    private OTPService otpService;

    @PostMapping("/sendotp")
    public ResponseEntity<?> sendOtp(@RequestBody EmailRequest emailRequest) {

        String email = emailRequest.getEmail();
        String otp = otpService.generateOTP(email);

        if (otp.contains("already")) {
            return ResponseEntity.status(409).body(
                new OTPresponseDTO(false, otp, null)
            );
        }
        return ResponseEntity.ok(
            new OTPresponseDTO(true, "OTP sent successfully", otp)
        );
    }

    @PostMapping("/signup")
    public ResponseEntity<SignUpReponseDto> register(@RequestBody SignUpRequestDto request){
        Object user = userService.register(request);

        if (user instanceof String) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new SignUpReponseDto(false, (String) user, null));
        }

        return ResponseEntity.ok(new SignUpReponseDto(true, "User is Registered", user));
        
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Users user , HttpServletResponse response){
        System.out.println(user);

        AuthResponse authResponse = userService.verify(user);

        if (authResponse == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "success", false,
                "message", "Wrong credentials or user not registered"
            ));
        }



        Cookie cookie = new Cookie("token", authResponse.getToken());
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // Set to false in development if not using HTTPS
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60);

        response.addCookie(cookie);

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Logged in successfully",
            "token", authResponse.getToken(),
            "existUser", authResponse.getUser()
        ));

    }

}
