package com.nhatquang.high_concurrency_ticketing_engine.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.nhatquang.high_concurrency_ticketing_engine.config.RabbitMQConfig;
import com.nhatquang.high_concurrency_ticketing_engine.dto.OrderMessage;
import com.nhatquang.high_concurrency_ticketing_engine.service.OrderService;
import com.nhatquang.high_concurrency_ticketing_engine.service.TicketService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j 
@Component
@RequiredArgsConstructor 
public class OrderTimeoutListener {
    
    private final OrderService orderService;
    private final TicketService ticketService;

    @RabbitListener(queues = RabbitMQConfig.ORDER_DELAY_QUEUE)
    public void processTimeout(OrderMessage message) {
        log.info("[Timeout Worker] Hết thời gian giữ chỗ. Đang kiểm tra trạng thái đơn hàng: {}", message.orderId());

        try {
            // 1. Kiểm tra và Hủy đơn hàng trong Database
            boolean isCanceled = orderService.cancelOrderIfPending(message.orderId());

            // 2. Nếu hủy thành công (tức là user chưa thanh toán), tiến hành hoàn vé về kho Redis
            if (isCanceled) {
                log.info("[Timeout Worker] Bắt đầu hoàn {} vé cho Ticket ID: {}", message.quantity(), message.ticketId());
                ticketService.refundTicket(message.ticketId(), message.quantity());
                log.info("[Timeout Worker] Xử lý hủy vé thành công!");
            } else {
                log.info("[Timeout Worker] Đơn hàng {} đã được thanh toán, không cần hoàn vé.", message.orderId());
            }
        } catch (Exception e) {
            log.error("[Timeout Worker] Lỗi khi xử lý timeout cho đơn hàng: {}", e.getMessage(), e);
            throw e;
        }
    }
}
