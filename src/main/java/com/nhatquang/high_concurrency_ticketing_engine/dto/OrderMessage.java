package com.nhatquang.high_concurrency_ticketing_engine.dto;

public record OrderMessage(
    Long userId,
    Long ticketId,
    int quantity
) {}