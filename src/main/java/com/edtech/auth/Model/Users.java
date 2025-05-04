package com.edtech.auth.Model;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
@Data
@Builder
public class Users {
    
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    private String firstName;

    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    // @Enumerated(EnumType.STRING)
    private String accountType;

    // public String getAccountType(){
    //     return accountType.name();
    // }

    private String image;  // Profile image URL or path

    private String token;

    private LocalDateTime resetPasswordExpires;

    private boolean active = true;

    private boolean approved = true;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Column(nullable = true)
    private String contactNumber;
    
     
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Users{");
        sb.append("id=").append(id);
        sb.append(", firstName=").append(firstName);
        sb.append(", lastName=").append(lastName);
        sb.append(", email=").append(email);
        sb.append(", password=").append(password);
        sb.append(", accountType=").append(accountType);
        sb.append(", image=").append(image);
        sb.append(", token=").append(token);
        sb.append(", resetPasswordExpires=").append(resetPasswordExpires);
        sb.append(", active=").append(active);
        sb.append(", approved=").append(approved);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append('}');
        return sb.toString();
    }

    @OneToOne
    private AdditionalDetails userProfile;

    @OneToMany
    private List<Course> EnrolledCourses;

    // 📈 Course Progress tracked for students
    @OneToMany
    private List<CourseProgress> courseProgress;

    
    
}
