package org.andarworld.kafkaservice1.usecases.dto;

public record ProductRequestDto(
        String uuid,
        String name,
        String quantity
) {
}
