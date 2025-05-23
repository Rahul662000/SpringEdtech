package com.edtech.auth.Model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;

@Data
@Entity
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String courseName;

    @Column(length = 1000)
    private String courseDescription;

    @ManyToOne
    @JoinColumn(name = "instructor_id", referencedColumnName = "id")
    // @JsonManagedReference("course-instructor")
    // @JsonBackReference
    @JsonIgnoreProperties({"courses", "enrolledCourses", "courseProgress", "userProfile"})
    private Users instructor;

    @Column(length = 1000)
    private String whatYouWillLearn;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonIgnoreProperties("course")
    // @JsonManagedReference
    private List<Section> courseContent = new ArrayList<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"course","user"})
    private List<RatingAndReview> ratingAndReview = new ArrayList<>();

    private Double price;

    private String thumbnail;

    @ElementCollection
    private List<String> tag;

    @ManyToOne
    @JoinColumn(name = "category_id")
    // @JsonBackReference
    @JsonBackReference
    private Category category;

    @ElementCollection
    private List<String> instructions;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt = new Date();

    public enum Status {
        Draft, Published
    }

    @Override
    public String toString() {
        return "Course [courseContent=" + courseContent + "]";
    }
    
    @ManyToMany
    private List<Users> enrolledStudents;

    


}
