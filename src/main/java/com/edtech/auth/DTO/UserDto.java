package com.edtech.auth.DTO;

import com.edtech.auth.Model.Users;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String accountType;
    private String image;
    private boolean active;
    private boolean approved;

    private String gender;
    private String dateOfBirth;
    private String about;
    private String contact;

    public UserDto(Users user) {
        this.id = user.getId();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.accountType = user.getAccountType();
        this.image = user.getImage();
        this.active = user.isActive();
        this.approved = user.isApproved();

        if (user.getUserProfile() != null) {
            this.gender = user.getUserProfile().getGender();
            this.dateOfBirth = user.getUserProfile().getDateOfBirth();
            this.about = user.getUserProfile().getAbout();
            this.contact = user.getUserProfile().getContactNumber();
        }
    }
}