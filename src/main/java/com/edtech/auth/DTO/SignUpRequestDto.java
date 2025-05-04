package com.edtech.auth.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequestDto {
    public String firstName;
    public String lastName;
    public String email;
    public String password;
    public String confirmPassword;
    public String accountType;
    public String contactNumber;
    public String otp;
}