package com.nhatquang.high_concurrency_ticketing_engine.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nhatquang.high_concurrency_ticketing_engine.entity.Order;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    
}
