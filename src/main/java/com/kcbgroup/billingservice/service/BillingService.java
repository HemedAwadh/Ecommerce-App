package com.kcbgroup.billingservice.service;

import com.kcbgroup.billingservice.entities.Billing;
import com.kcbgroup.billingservice.entities.Orders;
import com.kcbgroup.billingservice.entities.Products;
import com.kcbgroup.billingservice.model.OrderResponse;
import org.springframework.http.ResponseEntity;

public interface BillingService {
    Billing generateBilling(Orders orders, Products products);

    ResponseEntity<?> consumeMessage(OrderResponse orderResponse);
}
