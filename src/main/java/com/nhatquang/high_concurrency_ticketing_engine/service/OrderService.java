package com.nhatquang.high_concurrency_ticketing_engine.service;

import java.util.List;
import java.util.UUID;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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

    // Khi có đơn hàng mới, xóa cache danh sách vé của user này để hệ thống nạp lại
    @CacheEvict(value = "userOrders", key = "#message.userId()")
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

    // Khi hủy đơn, xóa toàn bộ cache để đảm bảo tính nhất quán (vì hàm này không nhận userId làm tham số)
    @CacheEvict(value = "userOrders", allEntries = true)
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

    // --- ISSUE 10: API Lấy danh sách (Có Cache) và Mock Thanh toán ---

    // Cache kết quả vào Redis. Chỉ truy vấn DB lần đầu tiên, các lần F5 sau sẽ lấy từ RAM
    @Cacheable(value = "userOrders", key = "#userId")
    public List getUserOrders(Long userId) {
        log.info("Truy vấn DB để lấy danh sách đơn hàng cho User ID: {} (Sẽ không log nếu đã được Cache)", userId);
        return orderRepository.findByUserIdOrderByCreateAtDesc(userId);
    }

    // Xóa Cache của user này khi thanh toán để cập nhật lại trạng thái PAID
    @CacheEvict(value = "userOrders", key = "#userId")
    @Transactional
    public void payOrder(UUID orderId, Long userId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng!"));

        // Bảo mật: Chỉ cho phép người tạo đơn được quyền thanh toán
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("Không có quyền truy cập đơn hàng này!");
        }

        if (!"PENDING".equals(order.getStatus())) {
            throw new RuntimeException("Đơn hàng không ở trạng thái chờ thanh toán!");
        }

        order.setStatus("PAID");
        orderRepository.save(order);
        log.info("Thanh toán thành công cho đơn hàng: {}", orderId);
    }
}
