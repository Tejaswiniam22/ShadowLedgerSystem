package com.shadowledger.eventservice;


import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;

import java.util.Optional;

import com.shadowledger.eventservice.entity.EventEntity;
import com.shadowledger.eventservice.model.EventRequest;
import com.shadowledger.eventservice.repository.EventRepository;
import com.shadowledger.eventservice.service.EventProducerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.kafka.core.KafkaTemplate;

class EventProducerServiceTest {

    private EventProducerService service;
    private EventRepository repository;
    private KafkaTemplate<String, EventRequest> kafkaTemplate;

    @BeforeEach
    void setup() {
        repository = Mockito.mock(EventRepository.class);
        kafkaTemplate = Mockito.mock(KafkaTemplate.class);
        service = new EventProducerService(kafkaTemplate, repository);
    }

    @Test
    void shouldPublishValidEvent() {
        EventRequest request = validRequest();

        Mockito.when(repository.findByEventId("E1000"))
                .thenReturn(Optional.empty());

        service.processEvent(request);

        Mockito.verify(repository, times(1))
                .save(Mockito.any(EventEntity.class));

        Mockito.verify(kafkaTemplate, times(1))
                .send("transactions.raw", "A20", request);
    }

    @Test
    void shouldRejectDuplicateEventId() {
        EventRequest request = validRequest();

        Mockito.when(repository.findByEventId("E1000"))
                .thenReturn(Optional.of(new EventEntity()));

        assertThrows(IllegalArgumentException.class,
                () -> service.processEvent(request));

        Mockito.verify(repository, times(0)).save(Mockito.any());
        Mockito.verify(kafkaTemplate, times(0))
                .send(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void shouldRejectInvalidType() {
        EventRequest request = validRequest();
        request.setType("invalid");

        assertThrows(IllegalArgumentException.class,
                () -> service.processEvent(request));
    }

    @Test
    void shouldPersistCorrectEntity() {
        EventRequest request = validRequest();

        Mockito.when(repository.findByEventId("E1000"))
                .thenReturn(Optional.empty());

        ArgumentCaptor<EventEntity> captor =
                ArgumentCaptor.forClass(EventEntity.class);

        service.processEvent(request);

        Mockito.verify(repository).save(captor.capture());

        EventEntity entity = captor.getValue();
        org.assertj.core.api.Assertions.assertThat(entity.getEventId()).isEqualTo("E1000");
        org.assertj.core.api.Assertions.assertThat(entity.getAccountId()).isEqualTo("A20");
        org.assertj.core.api.Assertions.assertThat(entity.getType()).isEqualTo("credit");
        org.assertj.core.api.Assertions.assertThat(entity.getAmount()).isEqualTo(500);
        org.assertj.core.api.Assertions.assertThat(entity.getTimestamp()).isEqualTo(1735561800000L);
    }

    private EventRequest validRequest() {
        EventRequest req = new EventRequest();
        req.setEventId("E1000");
        req.setAccountId("A20");
        req.setType("credit");
        req.setAmount(500);
        req.setTimestamp(1735561800000L);
        return req;
    }
}
