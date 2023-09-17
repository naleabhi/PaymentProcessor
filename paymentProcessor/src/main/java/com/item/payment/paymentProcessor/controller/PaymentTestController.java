package com.item.payment.paymentProcessor.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentTestController {

    @RequestMapping(method = RequestMethod.GET, value = "testPay")
    public String paymentTest()
    {
        return "Payment Test";
    }
}
