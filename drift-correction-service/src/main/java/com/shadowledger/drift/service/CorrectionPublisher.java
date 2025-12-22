package com.shadowledger.drift.service;

import com.shadowledger.drift.model.CorrectionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CorrectionPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public CorrectionPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(CorrectionEvent event) {
        log.info("Sending correction event to Kafka: {}", event);
        kafkaTemplate.send("transactions.corrections", event.getAccountId(), event);
    }
}

