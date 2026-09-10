package com.nhatquang.high_concurrency_ticketing_engine.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tickets")
@Getter 
@Setter
public class Ticket {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id")
    private Long eventId;

    @Column(name = "ticket_type")
    private String ticketType;

    private BigDecimal price;

    @Column(name = "total_stock")
    private Integer totalStock;

    @Column(name = "availableStock")
    private Integer availableStock;

    @Version
    private Integer version;
}
