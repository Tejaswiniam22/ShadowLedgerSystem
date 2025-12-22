//package com.shadowledger.ledger.consumer;
//
//import com.shadowledger.ledger.config.KafkaTopics;
//import com.shadowledger.ledger.domain.LedgerEntry;
//import com.shadowledger.ledger.service.ShadowLedgerService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Component;
//
//@Component
//@RequiredArgsConstructor
//public class TransactionConsumer {
//
//    private final ShadowLedgerService service;
//
//    @KafkaListener(topics = KafkaTopics.RAW)
//    public void consumeRaw(LedgerEntry entry) {
//        service.process(entry);
//    }
//
//    @KafkaListener(topics = KafkaTopics.CORRECTIONS)
//    public void consumeCorrection(LedgerEntry entry) {
//        service.process(entry);
//    }
//}
