package com.nhatquang.high_concurrency_ticketing_engine.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nhatquang.high_concurrency_ticketing_engine.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    
}
