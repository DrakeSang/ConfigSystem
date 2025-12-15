package com.emilyordanov.configupdateprocessor.kafka;

import com.emilyordanov.configupdateprocessor.event.ConfigurationChangeEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class ConfigurationChangeListener {
    @KafkaListener(topics = "configuration-changes", groupId = "config-update-processor")
    public void onMessage(
            @Payload ConfigurationChangeEvent event,
            @Header(KafkaHeaders.RECEIVED_KEY) String key) {

        System.out.println(
                "Kafka event received | key=" + key +
                        " | type=" + event.getEventType() +
                        " | app=" + event.getAppName() +
                        " | env=" + event.getEnv() +
                        " | version=" + event.getVersion() +
                        " | at=" + event.getTimestamp()
        );
    }
}
