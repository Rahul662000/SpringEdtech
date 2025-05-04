package com.edtech.auth.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AuthResponse {
    
    private String token;
    private UserDto user;

    // constructor
    public AuthResponse(String token, UserDto user) {
        this.token = token;
        this.user = user;
    }

}
