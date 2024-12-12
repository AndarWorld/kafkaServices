package org.andarworld.kafkaservice2.api.exceptions;

public class NonRetriableException extends RuntimeException {
    public NonRetriableException(String message) {
        super(message);
    }
}
