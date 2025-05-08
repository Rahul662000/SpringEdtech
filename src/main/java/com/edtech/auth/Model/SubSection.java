package com.edtech.auth.Model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import net.minidev.json.annotate.JsonIgnore;

@Data
@Entity
public class SubSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String timeDuration;
    private String description;
    private String videoUrl;

    @ManyToOne
    @JoinColumn(name = "section_id")
    @JsonIgnoreProperties
    @JsonBackReference
    private Section section;

}
