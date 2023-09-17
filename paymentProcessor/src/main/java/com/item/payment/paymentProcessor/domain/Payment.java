package com.item.payment.paymentProcessor.domain;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import java.io.Serializable;

@Entity
public class Payment implements Serializable{
    @EmbeddedId
    private PaymentId paymentId;

    private Integer price;

    public Payment() {

    }

    public PaymentId getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(PaymentId paymentId) {
        this.paymentId = paymentId;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "paymentId=" + paymentId +
                ", price=" + price +
                '}';
    }
}

