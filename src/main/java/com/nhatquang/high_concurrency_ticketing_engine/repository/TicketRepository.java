package com.nhatquang.high_concurrency_ticketing_engine.repository;

import com.nhatquang.high_concurrency_ticketing_engine.entity.Ticket;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Long>{
    
}
