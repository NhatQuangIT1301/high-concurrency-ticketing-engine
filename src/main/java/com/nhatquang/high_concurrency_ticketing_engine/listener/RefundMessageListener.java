package com.nhatquang.high_concurrency_ticketing_engine.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.nhatquang.high_concurrency_ticketing_engine.config.RabbitMQConfig;
import com.nhatquang.high_concurrency_ticketing_engine.dto.OrderMessage;
import com.nhatquang.high_concurrency_ticketing_engine.service.TicketService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor 
public class RefundMessageListener {
    
    private final TicketService ticketService;

    // Lắng nghe trực tiếp từ Dead Letter Queue
    @RabbitListener(queues = RabbitMQConfig.ORDER_DLQ)
    public void processRefund(OrderMessage message) {
        log.warn("[DLQ Consumer] Phát hiện đơn hàng lỗi do lưu DB thất bại. Bắt đầu hoàn vé cho User: {}, Ticket: {}", message.userId(), message.ticketId());

        try {
            // Thực thi bù trừ giao dịch (Compensating Transaction)
            ticketService.refundTicket(message.ticketId(), message.quantity());
            log.info("[DLQ Consumer] Xử lý bù trừ hoàn tất. Đã trả lại vé cho Ticket ID: {}", message.ticketId());
        } catch (Exception e) {
            log.error("[DLQ Consumer] Lỗi nghiêm trọng khi hoàn vé: {}", e.getMessage(), e);
        }
    }
}
