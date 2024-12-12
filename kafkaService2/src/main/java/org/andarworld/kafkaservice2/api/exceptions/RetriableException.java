package org.andarworld.kafkaservice2.api.exceptions;

public class RetriableException extends RuntimeException {
    public RetriableException(String message) {
        super(message);
    }
}
