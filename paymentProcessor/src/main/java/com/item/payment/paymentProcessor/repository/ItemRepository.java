package com.item.payment.paymentProcessor.repository;

import com.item.payment.paymentProcessor.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

}
