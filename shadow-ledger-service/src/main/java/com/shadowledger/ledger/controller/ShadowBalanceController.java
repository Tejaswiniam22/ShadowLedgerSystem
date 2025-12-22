package com.shadowledger.ledger.controller;

import com.shadowledger.ledger.domain.ShadowBalanceView;
import com.shadowledger.ledger.repository.LedgerEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/accounts")
@Slf4j
public class ShadowBalanceController {

    private final LedgerEntryRepository repo;

    @GetMapping("/{accountId}/shadow-balance")
    public ShadowBalanceView getBalance(@PathVariable String accountId) {

        List<Object[]> resultList = repo.computeShadowBalance(accountId);

        if (resultList.isEmpty()) {
            return new ShadowBalanceView(accountId, BigDecimal.ZERO, null);
        }

        Object[] row = resultList.get(0);

        String acctId = row[0] != null ? String.valueOf(row[0]) : accountId;

        BigDecimal balance = BigDecimal.ZERO;
        if (row.length > 1 && row[1] != null) {
            Object balanceObj = row[1];
            if (balanceObj instanceof Number) {
                balance = BigDecimal.valueOf(((Number) balanceObj).doubleValue());
            } else {
                throw new IllegalStateException("Cannot convert balance to BigDecimal: " + balanceObj);
            }
        }

        String lastEvent = row.length > 2 ? String.valueOf(row[2]) : null;

        return new ShadowBalanceView(acctId, balance, lastEvent);
    }
}
