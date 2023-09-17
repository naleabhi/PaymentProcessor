package com.item.payment.paymentProcessor.exception;

public class LowItemQuantityException extends RuntimeException {

    public LowItemQuantityException(String message) {
        super(message);
    }
}
