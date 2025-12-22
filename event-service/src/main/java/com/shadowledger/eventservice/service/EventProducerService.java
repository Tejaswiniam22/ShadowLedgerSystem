package com.shadowledger.eventservice.service;

import com.shadowledger.eventservice.entity.EventEntity;
import com.shadowledger.eventservice.model.EventRequest;
import com.shadowledger.eventservice.repository.EventRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class EventProducerService {

    private static final String TOPIC = "transactions.raw";

    private final KafkaTemplate<String, EventRequest> kafkaTemplate;
    private final EventRepository eventRepository;

    public EventProducerService(KafkaTemplate<String, EventRequest> kafkaTemplate,
                                EventRepository eventRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.eventRepository = eventRepository;
    }

    public void processEvent(EventRequest request) {
        if (!request.getType().equals("debit") && !request.getType().equals("credit")) {
            throw new IllegalArgumentException("Invalid type: " + request.getType());
        }

        eventRepository.findByEventId(request.getEventId())
                .ifPresent(e -> {
                    throw new IllegalArgumentException("Duplicate eventId");
                });

        EventEntity entity = new EventEntity();
        entity.setEventId(request.getEventId());
        entity.setAccountId(request.getAccountId());
        entity.setType(request.getType());
        entity.setAmount(request.getAmount());
        entity.setTimestamp(request.getTimestamp());

        eventRepository.save(entity);

        kafkaTemplate.send(TOPIC, request.getAccountId(), request);
    }
}

