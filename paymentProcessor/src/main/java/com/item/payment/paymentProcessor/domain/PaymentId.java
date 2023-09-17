package com.item.payment.paymentProcessor.domain;


import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class PaymentId implements Serializable {
    private long itemId;

    private String itemName;

    public PaymentId() {
    }

    public long getItemId() {
        return itemId;
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public PaymentId(long itemId, String itemName) {
        this.itemId = itemId;
        this.itemName = itemName;
    }

    @Override
    public String toString() {
        return "PaymentId{" +
                "itemId=" + itemId +
                ", itemName='" + itemName + '\'' +
                '}';
    }
}