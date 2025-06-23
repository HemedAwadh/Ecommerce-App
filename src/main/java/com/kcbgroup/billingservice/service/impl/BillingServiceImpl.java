package com.kcbgroup.billingservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcbgroup.billingservice.client.OrderClient;
import com.kcbgroup.billingservice.dto.SafaricomStkPushRequest;
import com.kcbgroup.billingservice.dto.SafaricomStkPushResponse;
import com.kcbgroup.billingservice.entities.Billing;
import com.kcbgroup.billingservice.entities.Orders;
import com.kcbgroup.billingservice.entities.Products;
import com.kcbgroup.billingservice.model.OrderResponse;
import com.kcbgroup.billingservice.repositories.BillingRepository;
import com.kcbgroup.billingservice.service.BillingService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillingServiceImpl implements BillingService {

    @Value("${safaricom.passkey}")
    private String passkey;

    private final BillingRepository billingRepository;
    private final OrderClient orderClient;
    private final ObjectMapper objectMapper;

    @Override
    public ResponseEntity<?> consumeMessage(OrderResponse orderResponse) throws JsonProcessingException {

        //1.Get access token

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        // Step 2: Initiate STK Push payment
        SafaricomStkPushRequest stkRequest = new SafaricomStkPushRequest();
        stkRequest.setBusinessShortCode("174379");
        stkRequest.setPassword(generatePassword("174379", timestamp, passkey));
        stkRequest.setTimestamp(timestamp);
        stkRequest.setTransactionType("CustomerPayBillOnline");
        stkRequest.setAmount((int) orderResponse.getPrimaryData().getTotalPrice());
        stkRequest.setPartyA(orderResponse.getPrimaryData().getCustomerPhone());
        stkRequest.setPartyB("174379");
        stkRequest.setPhoneNumber(orderResponse.getPrimaryData().getCustomerPhone());
        stkRequest.setCallbackURL("https://your-domain/api/callback/stkpush");
        stkRequest.setAccountReference("Payment");
        stkRequest.setTransactionDesc("Order Confirmation");

        log.info("Billing request to Safaricom : {}", objectMapper.writeValueAsString(stkRequest));

        SafaricomStkPushResponse response = orderClient.processBilling(stkRequest);

        log.info("Billing response from Safaricom : {}", response);

        //3.save billing information and transaction reference

        Billing billing = new Billing();
        billing.setProductId(orderResponse.getPrimaryData().getProductId());
        billing.setUnitPrice(Double.parseDouble(orderResponse.getPrimaryData().getProductPrice()));
        billing.setQuantity(orderResponse.getPrimaryData().getProductQuantity());
        billing.setTotalAmount(orderResponse.getPrimaryData().getTotalPrice());
        billing.setOrderId(orderResponse.getPrimaryData().getOrderId());
        billing.setStatus("PENDING");
        billing.setCheckoutRequestID(response.getCheckoutRequestID());
        billing.setMessage(response.getCustomerMessage());
        billingRepository.save(billing);


        return ResponseEntity.ok().body(response);


        //4.receive callback
        //5.mark the transaction as complete
        //6.send the message to queue to be consumed by notification service
    }

    private String generatePassword(String shortCode, String timestamp, String passkey) {
        String password = shortCode + passkey + timestamp;
        return Base64.getEncoder().encodeToString(password.getBytes());
    }

}
