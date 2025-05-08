package com.edtech.auth.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.edtech.auth.DTO.AuthResponse;
import com.edtech.auth.DTO.SignUpRequestDto;
import com.edtech.auth.DTO.UserDto;
import com.edtech.auth.Model.AdditionalDetails;
import com.edtech.auth.Model.OTP;
import com.edtech.auth.Model.Users;
import com.edtech.auth.Repo.AdditionalDetailsRepo;
import com.edtech.auth.Repo.OTPRepo;
import com.edtech.auth.Repo.UserRepo;

@Service
public class UserServices {
    
    @Autowired
    private UserRepo userRepo;

    @Autowired
    JWTService jwtService;

    @Autowired
    private OTPRepo otpRepo;

    @Autowired
    private AdditionalDetailsRepo additionalDetailsRepo;

    @Autowired
    AuthenticationManager authenticationManager;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);

    public Object register(SignUpRequestDto request){
        
        // 1. Validate required fields
        if (request.getFirstName() == null || request.getLastName() == null || request.getEmail() == null ||
            request.getPassword() == null || request.getConfirmPassword() == null || request.getOtp() == null) {
            return "All fields are required";
        }

        // 2. Check password match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            return "Password & Confirm Password do not match";
        }

        // 3. Check if user already exists
        if (userRepo.findByEmail(request.getEmail()) != null) {
            return "User already exists";
        }

        // 4. Find recent OTP for the email
        OTP recentOtp = otpRepo.findTop1ByEmailOrderByCreatedAtDesc(request.getEmail());

        if (recentOtp == null) {
            return "OTP not found";
        }

        if (recentOtp.getOtp() != Integer.parseInt(request.getOtp())) {
            return "Invalid OTP";
        }

        // 6. Generate default image
        String imageUrl = "https://api.dicebear.com/5.x/initials/svg?seed=" + request.getFirstName() + " " + request.getLastName();

        // 7. Set approval logic
        boolean approved = !request.getAccountType().equalsIgnoreCase("Instructor");

        // 8. Save user
        Users user = Users.builder()
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .email(request.getEmail())
            .password(encoder.encode(request.getPassword()))
            .accountType(request.getAccountType())
            .contactNumber(request.getContactNumber())
            .image(imageUrl)
            .approved(approved)
            // .userProfile(profile)
            .build();

        // 5. Create profile entry
        AdditionalDetails profile = AdditionalDetails.builder()
            .gender(null)
            .about(null)
            .contactNumber(request.getContactNumber())
            .dateOfBirth(null)
            .user(user)
            .build();

        user.setUserProfile(profile);

        userRepo.save(user);

        return user;

    }

    public AuthResponse verify(Users user) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword()));

        if(authentication.isAuthenticated()){
            Users existingUser = userRepo.findByEmail(user.getEmail());
            String token = jwtService.generateToken(existingUser.getEmail() , existingUser.getId() , existingUser.getAccountType());
            UserDto userDTO = new UserDto(existingUser);
            return new AuthResponse(token, userDTO);
        }

        return null;
    }

}
