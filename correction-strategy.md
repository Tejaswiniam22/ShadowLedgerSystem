# Drift Detection & Correction Strategy

## Drift Detection
The Drift & Correction Service compares:
- Reported CBS balance
- Shadow balance from ledger

## Drift Scenarios

### 1. No Drift
CBS balance equals shadow balance → no action.

### 2. Missing Credit
Shadow balance < CBS balance → generate credit correction.

### 3. Incorrect Debit
Shadow balance > CBS balance → generate debit reversal.

### 4. Unknown Cause
Mismatch exists but cannot be safely inferred → logged for manual review.

## Correction Events
Corrections are published as new events:
- Topic: `transactions.corrections`
- Type: `credit` or `debit`
- Amount: absolute difference
- Event ID prefixed with `CORR-`

## Manual Correction
Admins can trigger corrections explicitly:
POST /correct/{accountId}
This publishes a correction event directly to Kafka.
