package org.andarworld.kafkaservice1.api.exceptions;

public class NonRetriableException extends RuntimeException {
    public NonRetriableException(String message) {
        super(message);
    }
}
