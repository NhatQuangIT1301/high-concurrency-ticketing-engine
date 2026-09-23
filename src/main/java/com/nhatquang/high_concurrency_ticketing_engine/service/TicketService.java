package com.nhatquang.high_concurrency_ticketing_engine.service;

import java.util.Collections;
import java.util.UUID;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.nhatquang.high_concurrency_ticketing_engine.config.RabbitMQConfig;
import com.nhatquang.high_concurrency_ticketing_engine.dto.OrderMessage;
import com.nhatquang.high_concurrency_ticketing_engine.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final DefaultRedisScript<Long> decrementStockScript;
    private final DefaultRedisScript<Long> incrementStockScript; // Inject thêm script hoàn vé
    private final RabbitTemplate rabbitTemplate; // Inject thêm RabbitTemplate

    public boolean reserveTicketRedis(Long ticketId, int quantity) {
        // 1. Trừ tồn kho trên Redis
        // Tạo Redis Key chuẩn (Ví dụ: ticket:1:stock)
        String stockKey = "ticket:" + ticketId + ":stock";

        // Thực thi Lua Script
        Long result = redisTemplate.execute(
            decrementStockScript,
            Collections.singletonList(stockKey),    // Truyền vào danh sách KEYS
            String.valueOf(quantity)                // Truyền vào danh sách ARGV
        );

        // Xử lý kết quả trả về từ Script
        if (result == null || result == -1L) {
            log.error("Kho vé chưa được thiết lập cho Tiket ID: {}", ticketId);
            throw new RuntimeException("Lỗi hệ thống: Chưa khởi tạo kho vé!");
        }

        if (result == 0L) {
            log.warn("Vé ID {} đã sold out!", ticketId);
            return false; //Hết vé
        }

        log.info("Trừ thành công {} vé cho Ticket ID: {}", quantity, ticketId);

        // 2. Lấy thông tin User hiện tại từ SecurityContext
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = currentUser.getId();

        // 3. Đẩy message vào RabbitMQ để xử lý bất đồng bộ
        UUID orderId = UUID.randomUUID();
        OrderMessage message = new OrderMessage(orderId, userId, ticketId, quantity);

        try {
            // TIN NHẮN 1: Đẩy vào Queue chính để Worker lưu đơn hàng (Trạng thái PENDING)
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_EXCHANGE,
                RabbitMQConfig.ORDER_ROUTING_KEY,
                message
            );

            rabbitTemplate.convertAndSend(
                "order.delay.exchange",
                "order.delay.routing.key",
                message, 
                msg -> {
                    // Set độ trễ: 15 phút = 15 * 60 * 1000 mili-giây
                    // Để test nhanh ở local, em có thể sửa thành 10000 (10 giây) để xem kết quả ngay
                    msg.getMessageProperties().setDelayLong(10000L);
                    return msg;
                }
            );

            log.info("Đã gửi message tạo đơn hàng vào RabbitMQ cho User ID: {}, Ticket ID: {}", userId, ticketId);
        } catch (Exception e) {
            // Lưu ý kiến trúc: Nếu đẩy message lỗi, ta phải có cơ chế Retry hoặc lưu log để bồi hoàn (Compensating Transaction)
            // Ở phiên bản hiện tại, log lại lỗi (Dead Letter/Fallback xử lý sau)
            log.error("Lỗi khi gửi message vào RabbitMQ cho Ticket ID: {}", ticketId, e);
            throw new RuntimeException("Lỗi hệ thống: Không thể xử lý đơn hàng lúc này.");
        }

        return true; //Trừ vé thành công
    }

    public void refundTicket(Long ticketId, int quantity) {
        String stockKey = "ticket:" + ticketId + ":stock";

        Long result = redisTemplate.execute(
            incrementStockScript, 
            Collections.singletonList(stockKey), 
            String.valueOf(quantity)
        );

        if (result == null || result == -1) {
            log.error("CẢNH BÁO: Kho vé không tồn tại khi cố gắng hoàn {} vé cho Ticket ID: {}", quantity, ticketId);
            // Ở thực tế, ta sẽ gửi cảnh báo Telegram/Email cho Admin xử lý tay
        } else {
            log.info("Hoàn thành công {} vé vào Redis cho Ticket ID: {}", quantity, ticketId);
        }
    }
}
