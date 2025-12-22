package com.shadowledger.ledger.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventRequest {
    private String eventId;
    private String accountId;
    private String type;
    private BigDecimal amount;
    private Long timestamp;
}
