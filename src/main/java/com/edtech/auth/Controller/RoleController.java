package com.edtech.auth.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RoleController {

    @PreAuthorize("hasRole('Student')")
    @GetMapping("/student/dashboard")
    public ResponseEntity<String> studentDashboard() {
        return ResponseEntity.ok("Welcome Student! This is your dashboard.");
    }

    @PreAuthorize("hasRole('Instructor')")
    @GetMapping("/instructor/dashboard")
    public ResponseEntity<String> instructorDashboard() {
        return ResponseEntity.ok("Welcome Instructor! This is your dashboard.");
    }

    @PreAuthorize("hasRole('Admin')")
    @GetMapping("/admin/dashboard")
    public ResponseEntity<String> adminDashboard() {
        return ResponseEntity.ok("Welcome Admin! This is your dashboard.");
    }
    
}




// for change password need this 
// for delete profile auth/isstudent
// update , user all details , update display picture auth
// instructor dash aut/isinstructor


