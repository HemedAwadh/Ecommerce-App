package com.kcbgroup.billingservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kcbgroup.billingservice.entities.Billing;
import com.kcbgroup.billingservice.entities.Orders;
import com.kcbgroup.billingservice.entities.Products;
import com.kcbgroup.billingservice.model.OrderResponse;
import org.springframework.http.ResponseEntity;

public interface BillingService {

    ResponseEntity<?> consumeMessage(OrderResponse orderResponse) throws JsonProcessingException;
}
