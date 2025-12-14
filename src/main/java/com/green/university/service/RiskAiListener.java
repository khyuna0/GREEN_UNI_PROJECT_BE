package com.green.university.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
// E. RiskAiListener (커밋 후 실행 + 비동기)
public class RiskAiListener {

    private final AiAnalysisService aiAnalysisService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRiskChanged(RiskChangedEvent event) {
        try {
            aiAnalysisService.analyzeAndSaveMerged(event.riskId());
        } catch (Exception e) {
            log.error("AI 분석 실패 riskId={}", event.riskId(), e);
        }
    }
}
