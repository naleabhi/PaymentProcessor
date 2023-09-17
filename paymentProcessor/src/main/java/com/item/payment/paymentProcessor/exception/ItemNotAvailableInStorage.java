package com.item.payment.paymentProcessor.exception;

public class ItemNotAvailableInStorage extends RuntimeException {

    public ItemNotAvailableInStorage(String msg) {
       super(msg);
    }
}
