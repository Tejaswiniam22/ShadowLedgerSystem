package com.shadowledger.drift;

import com.shadowledger.drift.controller.ManualCorrectionController;
import com.shadowledger.drift.service.CorrectionPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;

class ManualCorrectionTest {

    private ManualCorrectionController controller;
    private CorrectionPublisher publisher;

    @BeforeEach
    void setup() {
        publisher = Mockito.mock(CorrectionPublisher.class);
        controller = new ManualCorrectionController(publisher);
    }

    @Test
    void shouldPublishManualCorrection() {
        controller.correct("A10", BigDecimal.valueOf(200));

        Mockito.verify(publisher, Mockito.times(1))
                .publish(Mockito.argThat(e -> e.getAccountId().equals("A10")
                        && e.getAmount().equals(BigDecimal.valueOf(200))
                        && e.getType().equals("credit")));
    }
}
