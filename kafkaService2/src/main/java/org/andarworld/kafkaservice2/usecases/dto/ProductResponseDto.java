package org.andarworld.kafkaservice2.usecases.dto;

import java.math.BigDecimal;

public record ProductResponseDto(
        String name,
        String quantity,
        BigDecimal price
) {
}
