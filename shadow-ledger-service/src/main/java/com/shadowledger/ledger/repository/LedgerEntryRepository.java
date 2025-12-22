package com.shadowledger.ledger.repository;

import com.shadowledger.ledger.domain.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {

    boolean existsByEventId(String eventId);

    @Query(value = """
    SELECT DISTINCT account_id,
           SUM(
             CASE WHEN type='credit' THEN amount ELSE -amount END
           ) OVER (
             PARTITION BY account_id
             ORDER BY timestamp, event_id
             ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
           ) AS balance,
           LAST_VALUE(event_id) OVER (
             PARTITION BY account_id
             ORDER BY timestamp, event_id
             ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING
           ) AS last_event
    FROM ledger_entries
    WHERE account_id = :accountId
    """, nativeQuery = true)
    List<Object[]> computeShadowBalance(@Param("accountId") String accountId);
}
