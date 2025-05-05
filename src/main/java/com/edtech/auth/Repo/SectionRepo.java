package com.edtech.auth.Repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.edtech.auth.Model.Section;

public interface SectionRepo extends JpaRepository<Section , Long>{
    
}
