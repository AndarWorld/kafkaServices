package org.andarworld.kafkaservice2.usecases.dto;

public record ProductRequestDto(
        String name,
        String quantity
) {
}
