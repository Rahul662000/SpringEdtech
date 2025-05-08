package com.edtech.auth.Repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.edtech.auth.Model.AdditionalDetails;

public interface AdditionalDetailsRepo extends  JpaRepository<AdditionalDetails , Long>{
    
    Optional<AdditionalDetails> findByUser_Id(Long userId);

}
