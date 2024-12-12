package org.andarworld.kafkaservice1.api.exceptions;

public class RetriableException extends RuntimeException {
    public RetriableException(String message) {
        super(message);
    }
}
