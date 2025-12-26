package com.green.university.domain.counseling.scheduler;

import com.green.university.domain.counseling.entity.ApprovalState;
import com.green.university.domain.counseling.entity.CounselingReserve;
import com.green.university.domain.counseling.entity.CounselingSchedule;
import com.green.university.domain.counseling.repository.CounselingReserveRepository;
import com.green.university.domain.dropoutrisk.entity.RiskStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class NoShowScheduler {

    private final CounselingReserveRepository reserveRepository;

        @Transactional
        @Scheduled(cron = "0 0 * * * *") // 매 정시
//        @Scheduled(cron = "0 */5 * * * *") // 5분 간격 (테스트용)
        public void markNoShow() {
            System.out.println("노쇼 감지 시작");
            LocalDate today = LocalDate.now();
            LocalDateTime now = LocalDateTime.now();

            List<CounselingReserve> targets =
                    reserveRepository
                            .findByApprovalStateAndCounselingSchedule_CounselingDate(
                                    ApprovalState.APPROVED, today
                            )
                            .stream()
                            .filter(r -> {
                                CounselingSchedule s = r.getCounselingSchedule();

                                LocalDateTime endDateTime =
                                        s.getCounselingDate()
                                                .atTime(s.getEndTime().intValue(), 0);

                                // 종료 + 10분 지났고, 아직 완료 처리 안 된 건
                                return endDateTime.plusMinutes(10).isBefore(now);

                            })
                            .toList();

            targets.forEach(r -> {
                returnDetected(r);
                r.setApprovalState(ApprovalState.NO_SHOW);
                }
            );
        }

        public void returnDetected(CounselingReserve counselingReserve) {
            if(counselingReserve.getDropoutRisk() != null && counselingReserve.getDropoutRisk().getStatus() != RiskStatus.RESOLVED) {
                // 위험 학생이 노쇼 했을 때, 완료 상태가 아닌 경우
                counselingReserve.getDropoutRisk().setStatus(RiskStatus.DETECTED);
            }

        }



    }

