package com.shadowledger.ledger.service;

import com.shadowledger.ledger.domain.EventType;
import com.shadowledger.ledger.domain.LedgerEntry;
import com.shadowledger.ledger.repository.LedgerEntryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class ShadowLedgerService {

    private final LedgerEntryRepository repo;

    public void process(LedgerEntry entry) {
        if (repo.existsByEventId(entry.getEventId())) {
            return;
        }

        BigDecimal balance = BigDecimal.ZERO;

        Object result = repo.computeShadowBalance(entry.getAccountId());

        if (result != null) {

            Object[] row = null;

            if (result instanceof Object[]) {
                Object[] outer = (Object[]) result;
                if (outer.length == 1 && outer[0] instanceof Object[]) {
                    row = (Object[]) outer[0];
                }
                else {
                    row = outer;
                }
            }

            if (row != null && row.length > 1 && row[1] instanceof BigDecimal) {
                balance = (BigDecimal) row[1];
            }
        }

        if (entry.getType() == EventType.debit &&
                balance.subtract(entry.getAmount()).signum() < 0) {
            throw new IllegalStateException("Negative balance not allowed");
        }

        repo.save(entry);
    }
}
