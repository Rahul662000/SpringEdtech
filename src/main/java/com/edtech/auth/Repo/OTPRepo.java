package com.edtech.auth.Repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.edtech.auth.Model.OTP;

public interface OTPRepo extends JpaRepository<OTP, Long>{
    
    Optional<OTP> findByEmail(String email);
    Optional<OTP> findByOtp(int otp);
    OTP findTop1ByEmailOrderByCreatedAtDesc(String email);
}
