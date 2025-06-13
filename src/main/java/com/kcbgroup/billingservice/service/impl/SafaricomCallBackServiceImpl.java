package com.kcbgroup.billingservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.kcbgroup.billingservice.configs.KafkaAppConfigs;
import com.kcbgroup.billingservice.dto.SafaricomStkCallback;
import com.kcbgroup.billingservice.entities.Billing;
import com.kcbgroup.billingservice.producer.KafkaMessagePublisher;
import com.kcbgroup.billingservice.repositories.BillingRepository;
import com.kcbgroup.billingservice.service.SafaricomCallBackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SafaricomCallBackServiceImpl implements SafaricomCallBackService {

    private final BillingRepository billingRepository;
    private final ObjectMapper objectMapper;
    private final KafkaMessagePublisher kafkaMessagePublisher;
    private final KafkaAppConfigs kafkaAppConfigs;


    @Override
    public ResponseEntity<?> processCallback(SafaricomStkCallback safaricomStkCallback) {

        try {

            var callBack = safaricomStkCallback.getBody().getStkCallback();
            var metaData = callBack.getCallbackMetadata();

            Double amount = null;
            String receipt = null;
            String date = null;
            String phone = null;

            for (var item : metaData.getItem()) {
                if ("Amount".equalsIgnoreCase(item.getName())) {
                    amount = (Double) item.getValue();
                } else if ("MpesaReceiptNumber".equalsIgnoreCase(item.getName())) {
                    receipt = (String) item.getValue();
                } else if ("TransactionDate".equalsIgnoreCase(item.getName())) {
                    date = (String) item.getValue();
                } else if ("PhoneNumber".equalsIgnoreCase(item.getName())) {
                    phone = (String) item.getValue();
                }
            }

            // 1. Save to DB and Update transaction status
            Billing billing = new Billing();
            billing.setCheckoutRequestID(callBack.getCheckoutRequestID());
            billing.setMessage(callBack.getResultDesc());
            billing.setMpesaReceiptNumber(receipt);
            billing.setTransactionDate(date);
            billing.setPhoneNumber(phone);
            billing.setTotalAmount(amount);
            billing.setStatus("Success");
            billingRepository.save(billing);


            // 3. Notify notification service (Kafka )
            String billingDto = objectMapper.writeValueAsString(billing);
            kafkaMessagePublisher.sendEventsToTopic(billingDto, kafkaAppConfigs.getNotificationTopic(), 3);
            log.info("******Successfully sent billing event*********");


            log.info("CallBack processed successfully");
            return ResponseEntity.ok().body(billingDto);
        } catch (Exception e) {
            log.error("Error processing Safaricom STK callback", e);
            return ResponseEntity.status(500).body("Internal Server Error");
        }
    }
}
