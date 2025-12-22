package com.shadowledger.drift.controller;

import com.shadowledger.drift.model.CorrectionEvent;
import com.shadowledger.drift.service.CorrectionPublisher;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/correct")
public class ManualCorrectionController {

    private final CorrectionPublisher publisher;

    public ManualCorrectionController(CorrectionPublisher publisher) {
        this.publisher = publisher;
    }

    @PostMapping("/{accountId}")
    public void correct(@PathVariable String accountId,
                        @RequestParam BigDecimal amount) {

        CorrectionEvent event = CorrectionEvent.builder()
                .eventId("MANUAL-" + UUID.randomUUID())
                .accountId(accountId)
                .type("credit")
                .amount(amount)
                .timestamp(System.currentTimeMillis())
                .build();

        publisher.publish(event);
    }
}
