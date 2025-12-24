package com.shadowledger.drift;

import com.shadowledger.drift.model.CbsBalance;
import com.shadowledger.drift.model.ShadowBalanceView;
import com.shadowledger.drift.service.CorrectionPublisher;
import com.shadowledger.drift.repository.ShadowLedgerRepository;
import com.shadowledger.drift.service.DriftDetectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Optional;

class DriftDetectionTest {

    private DriftDetectionService driftService;
    private ShadowLedgerRepository repository;
    private CorrectionPublisher publisher;

    @BeforeEach
    void setup() {
        repository = Mockito.mock(ShadowLedgerRepository.class);
        publisher = Mockito.mock(CorrectionPublisher.class);
        driftService = new DriftDetectionService(repository, publisher);
    }

    @Test
    void shouldDetectDriftAndPublishCorrection() {
        String accountId = "A10";
        BigDecimal shadowBalance = BigDecimal.valueOf(700);
        BigDecimal reported = BigDecimal.valueOf(750);

        Mockito.when(repository.findBalance(accountId))
                .thenReturn(Optional.of(new ShadowBalanceView(accountId, shadowBalance)));

        CbsBalance cbs = new CbsBalance();
        cbs.setAccountId(accountId);
        cbs.setReportedBalance(reported);

        driftService.checkAndCorrect(cbs);

        Mockito.verify(publisher, Mockito.times(1))
                .publish(Mockito.argThat(e -> e.getAccountId().equals(accountId)
                        && e.getAmount().equals(BigDecimal.valueOf(50))
                        && e.getType().equals("credit")));
    }
}
