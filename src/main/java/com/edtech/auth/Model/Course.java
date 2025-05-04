package com.edtech.auth.Model;

import java.util.Date;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
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
    private Users instructor;

    @Column(length = 1000)
    private String whatYouWillLearn;

    @OneToMany
    private List<Section> courseContent;

    @OneToMany(mappedBy = "course")
    private List<RatingAndReview> ratingAndReview;

    private Double price;

    private String thumbnail;

    @ElementCollection
    private List<String> tag;

    @ManyToOne
    private Category category;

    @ManyToMany
    private List<Users> enrolledStudents;

    @ElementCollection
    private List<String> instructions;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt = new Date();

    public enum Status {
        Draft, Published
    }
}
