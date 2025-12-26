package com.green.university.domain.counseling.scheduler;

import com.green.university.domain.counseling.entity.ApprovalState;
import com.green.university.domain.counseling.entity.CounselingReserve;
import com.green.university.domain.counseling.repository.CounselingReserveRepository;
import com.green.university.domain.counseling.repository.CounselingScheduleRepository;
import com.green.university.domain.dropoutrisk.entity.RiskStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class NotApprovedScheduleScheduler {

    private final CounselingReserveRepository reserveRepository;

    private final CounselingScheduleRepository scheduleRepository;

    // 만약 교수가 열어둔 일정에 대해 승인한 일정이 없는 상태로 startTime 이 지나버린 경우 (교수가 승인 안해주고 시간지남)
    // 신청 상태(approvalState) REJECTED로 변경, 위험학생인 경우 COUNSEL_REQ -> DETECTED 로 변경
    @Transactional
//    @Scheduled(cron = "0 0 * * * *") // 매 정시
  @Scheduled(cron = "0 */5 * * * *") // 5분 간격 (테스트용)
    public void markRejected () {

        System.out.println("미승인 예약 신청 감지 시작");
        LocalDate today = LocalDate.now();
        Long nowHour = (long) LocalTime.now().getHour();

        List<CounselingReserve> targets =
                reserveRepository
                        .findByApprovalStateAndCounselingSchedule_CounselingDateAndCounselingSchedule_StartTimeLessThanEqual(
                                ApprovalState.REQUESTED, today, nowHour
                        );
        targets.forEach(r -> {
            r.setApprovalState(ApprovalState.REJECTED);

            if (r.getDropoutRisk() != null && r.getDropoutRisk().getStatus() != RiskStatus.RESOLVED) {
                // 상담 완료 상태가 아닌 위험학생인 경우
                r.getDropoutRisk().setStatus(RiskStatus.DETECTED);
            }
        });

    }
}
