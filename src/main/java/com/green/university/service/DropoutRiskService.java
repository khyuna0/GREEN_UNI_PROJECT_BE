package com.green.university.service;

import com.green.university.dto.AiRiskAnalysisRequest;
import com.green.university.dto.RiskNotificationDto;
import com.green.university.dto.response.AiRiskAnalysisResult;
import com.green.university.entity.*;
import com.green.university.repository.DropoutRiskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DropoutRiskService {

    private final DropoutRiskRepository dropoutRiskRepository;
    private final AiAnalysisService aiAnalysisService; // AI API 호출 서비스
    private final GradeService gradeService;

    @Transactional
    public void evaluateAndAnalyzeRisk(StuSub stuSub, StuSubDetail detail) {
        // 1. 위험도 계산
        RiskType riskType = calculateRiskType(detail, stuSub);
        RiskLevel riskLevel = calculateRiskLevel(detail, stuSub);

        if (riskLevel == null) {
            // 위험 수준 없으면 기존 리스크를 RESOLVED 처리할 수도 있음
            return;
        }

        // 2. AI 입력 DTO 준비
        Double semesterGpa = gradeService.readMyGradeByStudentId(
                stuSub.getStudent().getId()
        ) != null ? (double) gradeService
                .readMyGradeByStudentId(stuSub.getStudent().getId())
                .getAverage() : null;

        AiRiskAnalysisRequest aiRequest = new AiRiskAnalysisRequest();
        aiRequest.setStudentId(stuSub.getStudent().getId());
        aiRequest.setStudentName(stuSub.getStudent().getName());
        aiRequest.setSubjectId(stuSub.getSubject().getId());
        aiRequest.setSubjectName(stuSub.getSubject().getName());
        aiRequest.setAbsent(detail.getAbsent());
        aiRequest.setLateness(detail.getLateness());
        aiRequest.setConvertedMark(detail.getConvertedMark());
        aiRequest.setGrade(detail.getGrade());
        aiRequest.setSemesterGpa(semesterGpa);
        aiRequest.setRiskType(riskType);
        aiRequest.setRiskLevel(riskLevel);

        String lastAiInput = buildLastAiInput(aiRequest);

        // 3. AI 호출
        AiRiskAnalysisResult aiResult = aiAnalysisService.analyzeRisk(aiRequest);

        // 4. DropoutRisk 생성/업데이트
        DropoutRisk risk = dropoutRiskRepository
                .findByStuSubAndRiskType(stuSub, riskType)
                .orElseGet(() -> {
                    DropoutRisk r = new DropoutRisk();
                    r.setStuSub(stuSub);
                    r.setRiskType(riskType);
                    return r;
                });

        risk.setRiskLevel(riskLevel);
        risk.setStatus(RiskStatus.DETECTED); // 상담 예약 전
        risk.setLastAiInput(lastAiInput);
        risk.updateFromAiResult(aiResult);

        dropoutRiskRepository.save(risk);
    }

    private RiskType calculateRiskType(StuSubDetail detail, StuSub stuSub) {
        // 단순 예시: 출결이 안 좋으면 ATTENDANCE, 그 외 SUBJECT_GRADE
        if (detail.getAbsent() != null && detail.getAbsent() >= 3) {
            return RiskType.ATTENDANCE;
        }
        return RiskType.SUBJECT_GRADE;
    }

    private RiskLevel calculateRiskLevel(StuSubDetail detail, StuSub stuSub) {
        long absent = detail.getAbsent() == null ? 0 : detail.getAbsent();
        long lateness = detail.getLateness() == null ? 0 : detail.getLateness();

        // 지각 3번 = 결석 1번
        long totalAbsent = absent + (lateness / 3);

        if (totalAbsent >= 5 || "F".equals(detail.getGrade())) {
            return RiskLevel.DANGER;
        }
        if (totalAbsent >= 3) {
            return RiskLevel.WARNING;
        }
        return null; // 위험 없음
    }

    private String buildLastAiInput(AiRiskAnalysisRequest req) {
        return String.format(
                "studentId=%d, subjectId=%d, absent=%d, lateness=%d, convertedMark=%.1f, grade=%s, semesterGpa=%s, riskType=%s, riskLevel=%s",
                req.getStudentId(),
                req.getSubjectId(),
                req.getAbsent(),
                req.getLateness(),
                req.getConvertedMark(),
                req.getGrade(),
                req.getSemesterGpa(),
                req.getRiskType(),
                req.getRiskLevel()
        );
    }
}



/**

    // 출석 위험 체크 (교수가 출석 입력할 때 호출)
    @Transactional
    public void checkAttendanceRisk(Student student, Subject subject, Long totalAbsent) {

        // 1. 출석 체크 로직 (지각3 = 결석1 변환 로직 포함)
        //Long totalAbsent = detail.getAbsent() + (detail.getLateness() / 3);

        // 결석 3회 이상 -> 위험 (AI 분석 필요)
        if (totalAbsent >= 3) {
            Long riskId = createRiskIfNotExists(student, subject, RiskType.ATTENDANCE, RiskLevel.DANGER);
            if (riskId != null) {
                // 트랜잭션 밖에서 AI 분석 실행
                executeAiAnalysis(riskId);
            }
        }
        // 2\결석 2회 -> 경고 (단순 알림)
        else if (totalAbsent == 2) {
            createRiskIfNotExists(student, subject, RiskType.ATTENDANCE, RiskLevel.WARNING);
        }

        // 2. 전체 학점 체크 로직 (성적 확정 시 호출)
        // ... (생략: 학생의 전체 평점 계산 후 3.0 미만이면 DANGER 저장)
    }


    // 성적 위험 체크 (학점 산출 시 호출)
    @Transactional
    public void checkGradeRisk(Student student, double currentGpa) {
        log.info("🎓 성적 체크 - studentId: {}, GPA: {}", student.getId(), currentGpa);

        if (currentGpa < 3.0) {
            // 성적 위험은 과목(Subject)이 null일 수 있음 (전체 평점이니까)
            Long riskId = createRiskIfNotExists(student, null, RiskType.GRADE, RiskLevel.DANGER);
            if (riskId != null) {
                executeAiAnalysis(riskId);
            }
        }
    }

    // 중복 체크 후 저장 로직
    @Transactional
    protected synchronized Long createRiskIfNotExists(Student student, Subject subject, RiskType type, RiskLevel level) {
        Long subjectId = (subject != null) ? subject.getId() : null;

        // 이미 같은 단계의 경고가 나가있는지 확인
        boolean exists = dropoutRiskRepository.findByStudent_IdAndSubject_IdAndRiskTypeAndRiskLevel(
                student.getId(), subjectId, type, level
        ).isPresent();

        if (!exists) {
            // 1. 일단 DropoutRisk 먼저 저장 (aiAnalysis는 null 상태)
            DropoutRisk risk = DropoutRisk.builder()
                    .student(student)
                    .subject(subject)
                    .riskType(type)
                    .riskLevel(level)
                    .status(RiskStatus.DETECTED)
                    .build();

            DropoutRisk savedRisk = dropoutRiskRepository.save(risk);
            log.info("DropoutRisk 저장 완료 - ID: {}", savedRisk.getId());
            // DANGER일 때만 riskId 반환
            return (level == RiskLevel.DANGER) ? savedRisk.getId() : null;
        }
        return null;
    }

    // ========== AI 분석 실행 (별도 트랜잭션) ==========
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void executeAiAnalysis(Long riskId) {
        log.info("🚀 AI 분석 요청 - riskId: {}", riskId);

        new Thread(() -> {
            try {
                Thread.sleep(500);  // 트랜잭션 커밋 대기

                RiskNotificationDto result = aiAnalysisService
                        .analyzeAndSaveWithMistral(riskId)
                        .block();  // 동기 실행

                if (result != null) {
                    log.info("✅ AI 분석 완료 - riskId: {}, message: {}",
                            result.getRiskId(),
                            result.getMessage());
                }

            } catch (Exception e) {
                log.error("❌ AI 분석 실패 - riskId: {}", riskId, e);
            }
        }).start();
    }
}

*/



//    private void createRiskIfNotExists(Student student, Subject subject, RiskType type, RiskLevel level, String reason) {
//        Long subjectId = (subject != null) ? subject.getId() : null;
//        boolean exists = riskRepository.findByStudent_IdAndSubject_IdAndRiskTypeAndRiskLevel(
//                student.getId(), subjectId, type, level
//        ).isPresent();
//        if (!exists) {
//            String aiAnalysisResult = null;
//            if (level == RiskLevel.DANGER) {
//                aiAnalysisResult = aiAnalysisService.generateConsultingGuide(student, subject, reason);
//            }
//            DropoutRisk risk = DropoutRisk.builder()
//                    .student(student)
//                    .subject(subject)
//                    .riskType(type)
//                    .riskLevel(level)
//                    .aiAnalysis(aiAnalysisResult)
//                    .status(RiskStatus.DETECTED)
//                    .build();
//            riskRepository.save(risk);
//            // ========== 알림 발송 ==========
//            notificationService.sendRiskAlertToStudent(risk);
//            if (subject != null) {
//                notificationService.sendRiskAlertToProfessor(risk);
//            }
//            // ==============================
//        }
//    }

