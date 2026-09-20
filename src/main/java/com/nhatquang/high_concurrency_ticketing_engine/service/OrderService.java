package com.nhatquang.high_concurrency_ticketing_engine.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nhatquang.high_concurrency_ticketing_engine.dto.OrderMessage;
import com.nhatquang.high_concurrency_ticketing_engine.entity.Order;
import com.nhatquang.high_concurrency_ticketing_engine.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final OrderRepository orderRepository;

    @Transactional
    public void createOrder(OrderMessage message) {
        log.info("Bắt đầu xử lý tạo đơn hàng cho User: {}, Ticket: {}", message.userId(), message.ticketId());

        try {
            Order order = Order.builder().userId(message.userId()).ticketId(message.ticketId()).quantity(message.quantity()).status("COMPLETE").build();
            
            orderRepository.save(order);
            log.info("Lưu đơn hàng thành công vào DB. User ID: {}, Ticket ID: {}", message.userId(), message.ticketId());
        } catch (Exception e) {
            log.error("Lỗi khi lưu đơn hàng vào DB: {}", e.getMessage(), e);
            throw e;
        }
    }
}
