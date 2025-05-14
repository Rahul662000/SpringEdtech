package com.edtech.auth.Repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.edtech.auth.Model.SubSection;

public interface SubSectionRepo extends JpaRepository<SubSection , Long>{

    List<SubSection> findBySectionId(Long id);
    
    

}
