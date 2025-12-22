# Ledger Ordering Rules

## Purpose
Accurate balance computation requires deterministic ordering of events.
Because Kafka does not guarantee global ordering across partitions, ordering
must be enforced explicitly.

## Ordering Strategy
Events are ordered using the following rules:

1. **Timestamp (ascending)**
2. **Event ID (ascending, lexicographical)**

This ensures:
- Reproducible balance calculations
- Correct replay of historical events
- Consistent results across restarts

## Implementation
Ordering is enforced at query time using SQL window functions:

```sql
ORDER BY timestamp, event_id
ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
