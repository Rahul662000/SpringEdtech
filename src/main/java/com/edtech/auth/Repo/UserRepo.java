package com.edtech.auth.Repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.edtech.auth.Model.Users;

public interface UserRepo extends  JpaRepository<Users, Long>{
    Users findByEmail(String email);
}
