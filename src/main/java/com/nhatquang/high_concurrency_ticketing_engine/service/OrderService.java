package com.nhatquang.high_concurrency_ticketing_engine.service;

import java.util.UUID;

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
            Order order = Order.builder().id(message.orderId()).userId(message.userId()).ticketId(message.ticketId()).quantity(message.quantity()).status("PENDING").build();
            
            orderRepository.save(order);
            log.info("Lưu đơn hàng thành công vào DB. User ID: {}, Ticket ID: {}", message.userId(), message.ticketId());
        } catch (Exception e) {
            log.error("Lỗi khi lưu đơn hàng vào DB: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public boolean cancelOrderIfPending(UUID orderId) {
        // Lấy đơn hàng từ DB lên
        Order order = orderRepository.findById(orderId).orElse(null);

        // Kiểm tra nếu đơn hàng tồn tại và vẫn đang ở trạng thái chờ thanh toán
        if (order != null && "PENDING".equals(order.getStatus())) {
            order.setStatus("CANCELED");
            orderRepository.save(order);
            log.info("Hủy thành công đơn hàng {} do quá hạn thanh toán.", orderId);
            return true; // Trả về true để báo cho Worker biết là cần hoàn vé
        }

        return false; // Nếu đã thanh toán (PAID) hoặc không tìm thấy thì bỏ qua
    }
}
