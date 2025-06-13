package com.kcbgroup.billingservice.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaMessagePublisher {

    private final KafkaTemplate template;


    public void sendEventsToTopic(String payload, String topic, int retryCount) {

        log.info("Sending event to topic :: " + topic + "-> with payload " + payload);
        try {

            retryCount += 1;

            CompletableFuture<SendResult<String, String>> future = template.send(topic, payload);
            int finalRetryCount = retryCount;
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("*** Sent message *** = [" + payload + "] with offset=[" + result.getRecordMetadata().offset() + "]");
                    log.info("** Sent message topic {}:::",topic);
                    log.info("*********************************-----------------------*******************************");
                } else {
                    log.info("Unable to send message = [" + payload + "] due to : " + ex.getMessage());
                    sendEventsToTopic(payload, topic, finalRetryCount);
                }
            });


        } catch (Exception ex) {
            ex.printStackTrace();
            log.error("ERROR :" + ex.getMessage());
        }
    }

}

