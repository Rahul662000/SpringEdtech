package com.edtech.auth.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateProfileDto {
    
    private String firstName;
    private String lastName;
    private String dateOfBirth;
    private String about;
    private String contactNumber;
    private String gender;

}
