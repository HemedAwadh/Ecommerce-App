package com.kcbgroup.billingservice.service.impl;

import com.kcbgroup.billingservice.client.OrderClient;
import com.kcbgroup.billingservice.dto.SafaricomStkPushRequest;
import com.kcbgroup.billingservice.entities.Billing;
import com.kcbgroup.billingservice.entities.Orders;
import com.kcbgroup.billingservice.entities.Products;
import com.kcbgroup.billingservice.model.OrderResponse;
import com.kcbgroup.billingservice.repositories.BillingRepository;
import com.kcbgroup.billingservice.service.BillingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillingServiceImpl implements BillingService {

    private final BillingRepository billingRepository;
    private final OrderClient orderClient;

    @Override
    public Billing generateBilling(Orders orders, Products products) {

        Billing billing = new Billing();
        billing.setBillingId(Long.valueOf(UUID.randomUUID().toString()));
        billing.setProductId(String.valueOf(products.getId()));
        billing.setOrderId(orders.getOrderId());
        billing.setQuantity(orders.getQuantity());
        billing.setUnitPrice(orders.getProductPrice());
        billing.setTotalAmount(products.getPrice() * orders.getQuantity());

        return billingRepository.save(billing);
    }

    @Override
    public ResponseEntity<?> consumeMessage(OrderResponse orderResponse) {

        //1.Get access token


        String timestamp = new SimpleDateFormat("YYYYMMDDHHmmss").format(new Date());

        // Step 2: Initiate STK Push payment
        SafaricomStkPushRequest stkRequest = new SafaricomStkPushRequest();
        stkRequest.setBusinessShortCode("174379");
        stkRequest.setPassword(generatePassword("174379", timestamp, "bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919"));
        stkRequest.setTimestamp(timestamp);
        stkRequest.setTransactionType("CustomerPayBillOnline");
        stkRequest.setAmount(String.valueOf(orderResponse.getPrimaryData().getTotalPrice()));
        stkRequest.setPartyA(orderResponse.getPrimaryData().getCustomerPhone());
        stkRequest.setPartyB("174379");
        stkRequest.setPhoneNumber(orderResponse.getPrimaryData().getCustomerPhone());
        stkRequest.setCallBackURL("https://your-domain/api/callback/stkpush");
        stkRequest.setAccountReference("Payment");
        stkRequest.setTransactionDesc("Order Confirmation");


        var response = orderClient.processBilling(stkRequest);

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


        return null;


        //4.receive callback
        //5.mark the transaction as complete
        //6.send the message to queue to be consumed by notification service
    }

    private String generatePassword(String shortCode, String timestamp, String passkey) {
        String strToEncode = shortCode + passkey + timestamp;
        return Base64.getEncoder().encodeToString(strToEncode.getBytes());
    }

}
