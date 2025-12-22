package com.shadowledger.ledger.domain;

import java.math.BigDecimal;

public record ShadowBalanceView(
        String accountId,
        BigDecimal balance,
        String lastEvent
) {}

