package com.edtech.auth.Config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

class SecurityConstants {
    public static final String[] PUBLIC_ENDPOINTS = {
        "/api/v1/auth/signup",
        "/api/v1/auth/login",
        "/sendMail",
        "/api/v1/auth/sendotp",
        "/",
        "/thumbnails/**",
        "/profilePicture/**",
        "/api/v1/course/showAllCategories",
        "/api/v1/course/getcategorypagedetails",
        "/api/v1/course/getCourseDetails",
        "/api/v1/course/getReviews",
        "/api/v1/course/getaveragerating"
    };

    public static final String[] STUDENT_ENDPOINT = {
        "/api/v1/payment/capturePayment",
        "/api/v1/payment/verifyPayment",
        "/api/v1/payment/sendPaymentSuccessfullEmail",
        "/api/v1/profile/getEnrolledCourses"
    };
    public static final String[] INSTRUCTOR_ENDPOINT = {
        "/api/v1/course/createCourse",
        "/api/v1/course/editCourse",
        "/api/v1/course/addSection",
        "/api/v1/course/addSubSection",
        "/api/v1/course/getInstructorCourses",
        "/api/v1/profile/instructor",
        "/api/v1/course/deleteCourse"

    };
    public static final String[] ADMIN_ENDPOINT = {"/api/v1/course/createcategory"};
}

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class securityConfig {

    @Autowired
    UserDetailsService userDetailsService;

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http.csrf(customizer -> customizer.disable());
        http
            .cors(Customizer.withDefaults())
            .authorizeHttpRequests(request -> request
                                    .requestMatchers(SecurityConstants.PUBLIC_ENDPOINTS)
                                    .permitAll()
                                    .requestMatchers(SecurityConstants.STUDENT_ENDPOINT).hasRole("STUDENT")
                                    .requestMatchers(SecurityConstants.INSTRUCTOR_ENDPOINT).hasRole("INSTRUCTOR")
                                    .requestMatchers(SecurityConstants.ADMIN_ENDPOINT).hasRole("ADMIN")
                                    .anyRequest().authenticated());
        http.httpBasic(Customizer.withDefaults());
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(new BCryptPasswordEncoder(10));
        provider.setUserDetailsService(userDetailsService);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception{

        return config.getAuthenticationManager();

    }
    
}
