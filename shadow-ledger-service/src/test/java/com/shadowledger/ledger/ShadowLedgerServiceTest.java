package com.shadowledger.ledger;

import com.shadowledger.ledger.domain.EventType;
import com.shadowledger.ledger.domain.LedgerEntry;
import com.shadowledger.ledger.repository.LedgerEntryRepository;
import com.shadowledger.ledger.service.ShadowLedgerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ShadowLedgerServiceTest {

    @Autowired
    private LedgerEntryRepository repo;

    @Autowired
    private ShadowLedgerService service;

    @Test
    void shouldRejectNegativeBalanceOnDebit() {
        LedgerEntry credit = new LedgerEntry();
        credit.setEventId("E3");
        credit.setAccountId("A11");
        credit.setType(EventType.credit);
        credit.setAmount(BigDecimal.valueOf(50));
        credit.setTimestamp(Instant.now());

        LedgerEntry debit = new LedgerEntry();
        debit.setEventId("E4");
        debit.setAccountId("A11");
        debit.setType(EventType.debit);
        debit.setAmount(BigDecimal.valueOf(100)); // exceeds balance
        debit.setTimestamp(Instant.now().plusSeconds(1));

        service.process(credit);

        // Expect IllegalStateException
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.process(debit));
        assertEquals("Negative balance not allowed", ex.getMessage());

        // Balance should remain 50 after failed debit
        List<Object[]> result = repo.computeShadowBalance("A11");
        Object[] lastRow = result.get(result.size() - 1);
        BigDecimal balance = BigDecimal.ZERO;
        if (lastRow.length > 1 && lastRow[1] instanceof Number) {
            balance = BigDecimal.valueOf(((Number) lastRow[1]).doubleValue());
        }
        assertEquals(0, balance.compareTo(BigDecimal.valueOf(50)));
    }

    @Test
    void shouldEnforceIdempotency() {
        LedgerEntry e1 = new LedgerEntry();
        e1.setEventId("E5");
        e1.setAccountId("A12");
        e1.setType(EventType.credit);
        e1.setAmount(BigDecimal.valueOf(100));
        e1.setTimestamp(Instant.now());

        LedgerEntry duplicate = new LedgerEntry();
        duplicate.setEventId("E5"); // same eventId
        duplicate.setAccountId("A12");
        duplicate.setType(EventType.credit);
        duplicate.setAmount(BigDecimal.valueOf(100));
        duplicate.setTimestamp(Instant.now().plusSeconds(1));

        service.process(e1);
        service.process(duplicate); // should be ignored

        // Final balance should still be 100
        List<Object[]> result = repo.computeShadowBalance("A12");
        Object[] lastRow = result.get(result.size() - 1);
        BigDecimal balance = BigDecimal.ZERO;
        if (lastRow.length > 1 && lastRow[1] instanceof Number) {
            balance = BigDecimal.valueOf(((Number) lastRow[1]).doubleValue());
        }

        assertEquals(0, balance.compareTo(BigDecimal.valueOf(100)));
    }
}
