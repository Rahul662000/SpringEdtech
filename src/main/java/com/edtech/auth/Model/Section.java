package com.edtech.auth.Model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Data;

@Data
@Entity
public class Section {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sectionName;

    @OneToMany(mappedBy = "section", orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<SubSection> subSection = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "course_id")
    // @JsonIgnoreProperties
    @JsonBackReference
    private Course course;

}
