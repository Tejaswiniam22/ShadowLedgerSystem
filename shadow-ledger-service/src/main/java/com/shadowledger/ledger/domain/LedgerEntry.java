package com.shadowledger.ledger.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = "ledger_entries",
        uniqueConstraints = @UniqueConstraint(columnNames = "eventId")
)
@Getter
@Setter
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String eventId;
    private String accountId;

    @Enumerated(EnumType.STRING)
    private EventType type;

    private BigDecimal amount;
    private Instant timestamp;
}
