package com.nhatquang.high_concurrency_ticketing_engine.service;

import java.util.Collections;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final DefaultRedisScript<Long> decrementStockScript;

    public boolean reserveTicketRedis(Long ticketId, int quantity) {
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
        return true; //Trừ vé thành công
    }
}
