package com.nhatquang.high_concurrency_ticketing_engine.dto;

import java.util.UUID;

public record OrderMessage(
    UUID orderId,
    Long userId,
    Long ticketId,
    int quantity
) {}