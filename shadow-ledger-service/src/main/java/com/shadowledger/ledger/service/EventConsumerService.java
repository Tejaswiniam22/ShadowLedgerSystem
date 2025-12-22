package com.shadowledger.ledger.service;

import com.shadowledger.ledger.dto.EventRequest;
import com.shadowledger.ledger.domain.LedgerEntry;
import com.shadowledger.ledger.domain.EventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventConsumerService {

    private final ShadowLedgerService shadowLedgerService;

    @KafkaListener(topics = "transactions.raw", groupId = "shadow-ledger-group")
    public void consume(EventRequest request) {
        log.info("Consuming event: {}", request.getEventId());

        try {
            LedgerEntry entry = new LedgerEntry();
            entry.setEventId(request.getEventId());
            entry.setAccountId(request.getAccountId());
            entry.setType(EventType.valueOf(request.getType()));
            entry.setAmount(request.getAmount()); // ⭐ Already BigDecimal
            entry.setTimestamp(Instant.ofEpochMilli(request.getTimestamp() != null
                    ? request.getTimestamp()
                    : System.currentTimeMillis())); // ⭐ Handle null

            shadowLedgerService.process(entry);

            log.info("Successfully processed event for account: {}", request.getAccountId());
        } catch (IllegalStateException e) {
            log.error("Business rule violation: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Error processing event: {}", request.getEventId(), e);
            throw e;
        }
    }

    @KafkaListener(topics = "transactions.corrections", groupId = "shadow-ledger-corrections")
    public void consumeCorrection(EventRequest request) {
        log.info("Consuming CORRECTION event: {} ", request.getEventId());
        consume(request);
    }

}