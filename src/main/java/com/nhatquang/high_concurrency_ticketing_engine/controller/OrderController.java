package com.nhatquang.high_concurrency_ticketing_engine.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nhatquang.high_concurrency_ticketing_engine.entity.User;
import com.nhatquang.high_concurrency_ticketing_engine.service.OrderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/orders")
@RequiredArgsConstructor
public class OrderController {
    
    private final OrderService orderService;

    // Lấy danh sách đơn hàng của tôi
    @GetMapping("/my-orders")
    public ResponseEntity getMyOrders() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List orders = orderService.getUserOrders(currentUser.getId());
        return ResponseEntity.ok(orders);
    }

    // Giả lập thanh toán đơn hàng
    @PostMapping("/{orderId}/pay")
    public ResponseEntity payOrder(@PathVariable UUID orderId) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        try {
            orderService.payOrder(orderId, currentUser.getId());
            return ResponseEntity.ok("Thanh toán thành công đơn hàng " + orderId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
