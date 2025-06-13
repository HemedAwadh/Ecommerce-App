package com.kcbgroup.billingservice.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcbgroup.billingservice.model.OrderResponse;
import com.kcbgroup.billingservice.service.BillingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumerService {
    private final ObjectMapper objectMapper;
    private final BillingService billingService;


    @KafkaListener(topics = "${spring.kafka.order-topic}")
    public void consumeReceiverTopic(String message) throws JsonProcessingException {

        log.info("=====================================================================");
        log.info( " INCOMING REQUEST TO BILLING SERVICE{}:: " + message, "");
        log.info("=====================================================================");

        log.info("INCOMING OBJECT REQUEST ::" + message);


        var result = objectMapper.readValue(message, OrderResponse.class);

        log.info("INCOMING OBJECT REQUEST ::" + result);


        billingService.consumeMessage(result);


    }

}
