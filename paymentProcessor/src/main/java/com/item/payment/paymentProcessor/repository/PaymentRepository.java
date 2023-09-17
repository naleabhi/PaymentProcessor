package com.item.payment.paymentProcessor.repository;

import com.item.payment.paymentProcessor.domain.Payment;
import com.item.payment.paymentProcessor.domain.PaymentId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, PaymentId> {

}
