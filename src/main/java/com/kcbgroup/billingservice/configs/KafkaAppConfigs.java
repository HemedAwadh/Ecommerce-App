package com.kcbgroup.billingservice.configs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author - KEN22967
 * KCB GROUP - BSS
 * Project -
 */

@Configuration

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class KafkaAppConfigs {

    @Value("${spring.kafka.order-topic}")
    private String orderTopic;

    @Value("${spring.kafka.notification-topic}")
    private String notificationTopic;


}
