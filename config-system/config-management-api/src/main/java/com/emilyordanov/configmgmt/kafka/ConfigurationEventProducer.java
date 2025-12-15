package com.emilyordanov.configmgmt.kafka;

import com.emilyordanov.configmgmt.event.ConfigurationChangeEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ConfigurationEventProducer {
    private static final String TOPIC = "configuration-changes";

    private final KafkaTemplate<String, ConfigurationChangeEvent> kafkaTemplate;

    public ConfigurationEventProducer(KafkaTemplate<String, ConfigurationChangeEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(ConfigurationChangeEvent event) {
        String key = event.getAppName() + ":" + event.getEnv();
        kafkaTemplate.send(TOPIC, key, event);
    }
}
