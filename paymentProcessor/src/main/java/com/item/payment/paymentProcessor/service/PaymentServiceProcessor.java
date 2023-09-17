package com.item.payment.paymentProcessor.service;

import com.common.retryProcess.configuration.EmailDetails;
import com.common.retryProcess.service.NotificationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.item.payment.paymentProcessor.domain.Item;
import com.item.payment.paymentProcessor.domain.Payment;
import com.item.payment.paymentProcessor.domain.PaymentId;
import com.item.payment.paymentProcessor.exception.ItemNotAvailableInStorage;
import com.item.payment.paymentProcessor.exception.LowItemQuantityException;
import com.item.payment.paymentProcessor.repository.ItemRepository;
import com.item.payment.paymentProcessor.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class PaymentServiceProcessor {

    Logger log= LoggerFactory.getLogger(PaymentServiceProcessor.class);
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private EmailDetails emailDetails;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private PaymentRepository paymentRepository;
    private int totalAmount;

    String subjectLine="****The Bill receipt****";
    private Map<String,Integer> itemMap= new HashMap<>();
    @KafkaListener(groupId = "${spring.kafka.consumer.group-id}",topics = "${spring.kafka.template.default-topic}", containerFactory = "concurrentKafkaListenerContainerFactory")
    private void consumeItem(String itemMsg) throws LowItemQuantityException, ItemNotAvailableInStorage, JsonProcessingException {
        Item item= mapper.readValue(itemMsg, Item.class);
        log.info("Item Read from topic with value: {}",item);
        if(item.getItemId()!=0 && item.getQuantity()!=0) {
            boolean itemAvailable = checkStorage(item);
            if (itemAvailable) {
                int itemPrice = calculatePayment(item);
                totalAmount += itemPrice;
                itemMap.put(item.getItemName(), item.getQuantity());
            }
        }else {
            log.info("Total Item Kart:");
            itemMap.entrySet().stream().forEach(a-> log.info("\nItem Name: {}, Item Quantity: {}",a.getKey(), a.getValue()));
            log.info("\nTotal Cost: {}",totalAmount);
            emailDetails.setToAddress(new String[]{item.getItemName()});
            emailDetails.setSubject(subjectLine);
            notificationService.sendEmailReceipt(itemMap, emailDetails, totalAmount );
            itemMap= new HashMap<>();
            totalAmount=0;
        }

    }

    private int calculatePayment(Item item) {
        PaymentId paymentId= new PaymentId(item.getItemId(), item.getItemName());
        Optional<Payment> payment=paymentRepository.findById(paymentId);
        return payment.get().getPrice() * item.getQuantity();
    }

    private boolean checkStorage(Item item) throws ItemNotAvailableInStorage {

       Optional<Item> storageItem= itemRepository.findById(item.getItemId());
       boolean storage;
       if(storageItem.isPresent())
       {
           if(item.getQuantity()<storageItem.get().getQuantity())
           {
              int quantity= storageItem.get().getQuantity()-item.getQuantity();
              storageItem.get().setQuantity(quantity);
              itemRepository.save(storageItem.get());
              storage=true;
           }else {
               throw new LowItemQuantityException(storageItem.get().toString());
           }
       }else {
           throw new ItemNotAvailableInStorage(item.toString());
       }
        return storage;
    }
}
