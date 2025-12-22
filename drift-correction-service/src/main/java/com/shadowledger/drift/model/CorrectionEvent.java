package com.shadowledger.drift.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Builder
@ToString
public class CorrectionEvent {
    private String eventId;
    private String accountId;
    private String type; // credit / debit
    private BigDecimal amount;
    private Long timestamp;
}
