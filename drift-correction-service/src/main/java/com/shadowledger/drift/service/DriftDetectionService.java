package com.shadowledger.drift.service;

import com.shadowledger.drift.model.CbsBalance;
import com.shadowledger.drift.model.CorrectionEvent;
import com.shadowledger.drift.repository.ShadowLedgerRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DriftDetectionService {

    private final ShadowLedgerRepository repository;
    private final CorrectionPublisher publisher;

    public DriftDetectionService(ShadowLedgerRepository repository,
                                 CorrectionPublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    public void checkAndCorrect(CbsBalance cbs) {
        log.info("Checking drift for accountId={} reportedBalance={}",
                cbs.getAccountId(), cbs.getReportedBalance());

        var shadow = repository.findBalance(cbs.getAccountId());
        if (shadow.isEmpty()) {
            log.warn("No shadow balance found for accountId={}", cbs.getAccountId());
            return;
        }

        BigDecimal diff = cbs.getReportedBalance().subtract(shadow.get().getBalance());

        if (diff.compareTo(BigDecimal.ZERO) == 0) {
            log.info("No drift detected for accountId={}", cbs.getAccountId());
            return;
        }

        CorrectionEvent event = CorrectionEvent.builder()
                .eventId("CORR-" + UUID.randomUUID())
                .accountId(cbs.getAccountId())
                .type(diff.signum() > 0 ? "credit" : "debit")
                .amount(diff.abs())
                .build();

        log.info("Drift detected! Publishing correction event={}", event);

        publisher.publish(event);
    }
}


