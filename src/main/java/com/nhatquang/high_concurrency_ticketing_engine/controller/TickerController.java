package com.nhatquang.high_concurrency_ticketing_engine.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.nhatquang.high_concurrency_ticketing_engine.service.TicketService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;


@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor 
public class TickerController {
    
    private final TicketService ticketService;

    // API: POST http://localhost:8080/api/tickets/1/reserve?quantity=2
    @PostMapping("/{ticketId}/reserve")
    public ResponseEntity<String> reserveTicket(
            @PathVariable Long ticketId,
            @RequestParam(defaultValue = "1") int quantity) {
        
        try {
            // Gọi xuống Service để chạy Lua Script trên Redis
            boolean isSuccess = ticketService.reserveTicketRedis(ticketId, quantity);

            if (isSuccess) {
                return ResponseEntity.ok("Mua thành công " + quantity + " vé cho Ticket Id: " + ticketId);
            } else {
                return ResponseEntity.badRequest().body("Rất tiếc, vé đã sold out!");
            }
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
