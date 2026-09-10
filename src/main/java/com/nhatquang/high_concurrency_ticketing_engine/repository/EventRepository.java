package com.nhatquang.high_concurrency_ticketing_engine.repository;

import com.nhatquang.high_concurrency_ticketing_engine.entity.Event;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {
    
}
