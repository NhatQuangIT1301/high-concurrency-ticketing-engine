package com.nhatquang.high_concurrency_ticketing_engine.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity 
@Table(name = "orders", indexes = {
    //Đánh index để truy vấn nhanh các đơn hàng quá hạn 10 phút
    @Index(name = "idx_orders_created_at", columnList = "created_at")
})
@Getter 
@Setter 
public class Order {
    
    @Id 
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "ticket_id")
    private Long ticketId;

    private Integer quantity;

    private String status;

    @Column(name = "created_at")
    private LocalDateTime createAt;
}
