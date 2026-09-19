package com.nhatquang.high_concurrency_ticketing_engine.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.nhatquang.high_concurrency_ticketing_engine.config.RabbitMQConfig;
import com.nhatquang.high_concurrency_ticketing_engine.dto.OrderMessage;
import com.nhatquang.high_concurrency_ticketing_engine.service.OrderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderMessageListener {
    
    private final OrderService orderService;

    // Lắng nghe trực tiếp từ hàng đợi đã khai báo trong cấu hình
    @RabbitListener(queues = RabbitMQConfig.ORDER_QUEUE)
    public void processOrder(OrderMessage message) {
        log.info("[RabbitMQ Consumer] Nhận được message: {}", message);

        try {
            // Đẩy sang Service để xử lý logic lưu DB
            orderService.createOrder(message);
            log.info("[RabbitMQ Consumer] Xử lý message thành công!");
        } catch (Exception e) {
            log.error("[RabbitMQ Consumer] Xử lý message thất bài: {}", e.getMessage());
            // Trong thực tế, nếu throw exception ở đây, RabbitMQ có thể tự động requeue (đẩy lại vào hàng đợi)
            throw e;
        }
    }
}
