package com.green.university.infra.ai;

import com.green.university.domain.grade.service.GradeService;
import com.green.university.domain.subject.entity.StuSub;
import com.green.university.domain.subject.entity.StuSubDetail;
import com.green.university.infra.ai.dto.AiRiskAnalysisRequest;
import com.green.university.infra.ai.dto.response.AiRiskAnalysisResult;
import com.green.university.infra.ai.entity.DropoutRisk;
import com.green.university.infra.ai.entity.RiskLevel;
import com.green.university.infra.ai.entity.RiskStatus;
import com.green.university.infra.ai.entity.RiskType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DropoutRiskService {

    private final DropoutRiskRepository dropoutRiskRepository;
    private final AiAnalysisService aiAnalysisService; // AI API 호출 서비스
    private final GradeService gradeService;


    // 성적/출결 변경 시 호출되는 메인 메서드
    @Transactional
    public void evaluateAndAnalyzeRisk(StuSub stuSub, StuSubDetail detail) {

        // 1. 위험도 계산 (비즈니스 로직)
        RiskAnalysis analysis = calculateRisk(detail);

        // 위험이 없으면(NULL) 기존 리스크가 있다면 해결(RESOLVED) 처리 후 종료하거나, 그냥 종료
        if (analysis == null) {
            // 필요하다면 여기서 기존 DETECTED 상태인 리스크를 찾아서 RESOLVED로 바꾸는 로직 추가 가능
            return;
        }

        // 2. AI 분석 요청 데이터 생성
        // 직전 학기 평점 가져오기 (없으면 0.0)
        Double prevGpa = 0.0;
        try {
            // GradeService의 로직에 따라 약간 다를 수 있음 (Null 체크 필수)
            var myGrade = gradeService.readMyGradeByStudentId(stuSub.getStudent().getId());
            if (myGrade != null) prevGpa = (double) myGrade.getAverage();
        } catch (Exception e) {
            log.warn("GPA 조회 실패 (신입생 등): {}", e.getMessage());
        }

        AiRiskAnalysisRequest request = AiRiskAnalysisRequest.builder()
                .studentId(stuSub.getStudent().getId())
                .studentName(stuSub.getStudent().getName())
                .subjectId(stuSub.getSubject().getId())
                .subjectName(stuSub.getSubject().getName())
                .absent(detail.getAbsent())
                .lateness(detail.getLateness())
                .convertedMark(detail.getConvertedMark())
                .grade(detail.getGrade())
                .semesterGpa(prevGpa)
                .riskType(analysis.type)
                .riskLevel(analysis.level)
                .build();

        // 3. AI 서비스 호출 (오래 걸릴 수 있으므로 비동기로 빼는게 좋지만, 일단 동기로 진행)
        AiRiskAnalysisResult aiResult = aiAnalysisService.analyzeRisk(request);

        // 4. DB 저장 (Update or Insert)
        DropoutRisk risk = dropoutRiskRepository.findByStuSubAndRiskType(stuSub, analysis.type)
                .orElseGet(() -> DropoutRisk.builder()
                        .stuSub(stuSub)
                        .riskType(analysis.type)
                        .build());

        // 내용 업데이트
        risk.setRiskLevel(analysis.level);
        risk.setStatus(RiskStatus.DETECTED); // 다시 위험 감지됨
        risk.setLastAiInput(request.toString()); // 혹은 핵심 요약만

        // AI 결과 매핑
        risk.setAiSummary(aiResult.getSummary());
        risk.setAiRecommendation(aiResult.getProfessorGuide());
        risk.setAiStudentMessage(aiResult.getStudentMessage());
        // 태그 리스트는 String으로 변환해서 저장 (예: "결석,성적")
        if (aiResult.getReasonTags() != null) {
            risk.setAiReasonTags(String.join(",", aiResult.getReasonTags()));
        }

        dropoutRiskRepository.save(risk);
        log.info("학생({}) 위험 분석 저장 완료: {}", stuSub.getStudent().getName(), analysis.type);
    }

    // 내부적으로 쓰는 위험 분석 결과 클래스
    private record RiskAnalysis(RiskType type, RiskLevel level) {
    }

    // 위험도 계산 로직 분리
    private RiskAnalysis calculateRisk(StuSubDetail detail) {
        long absent = detail.getAbsent() == null ? 0 : detail.getAbsent();
        long lateness = detail.getLateness() == null ? 0 : detail.getLateness();
        long totalAbsent = absent + (lateness / 3);
        // 지각 3회 = 결석 1회로 치환한 총 결석 수
        String grade = detail.getGrade();
        boolean attendanceDanger = totalAbsent >= 5;
        boolean gradeDanger = "F".equalsIgnoreCase(grade);

        // 1. 위험 레벨: DANGER (결석 5회 이상 OR F학점)
        // DANGER
        if (attendanceDanger && gradeDanger) {
            return new RiskAnalysis(RiskType.BOTH, RiskLevel.DANGER);
        }
        if (attendanceDanger) {
            return new RiskAnalysis(RiskType.ATTENDANCE, RiskLevel.DANGER);
        }
        if (gradeDanger) {
            return new RiskAnalysis(RiskType.SUBJECT_GRADE, RiskLevel.DANGER);
        }

        // 2. 위험 레벨: WARNING (결석 3회 이상 OR 점수가 낮음 등)
        if (totalAbsent >= 3) {
            return new RiskAnalysis(RiskType.ATTENDANCE, RiskLevel.WARNING);
        }

        // 성적 경고 기준 (예: C+ 이하이거나 환산점수 70점 미만 - 규칙은 정하기 나름)
        if (detail.getConvertedMark() != null && detail.getConvertedMark() < 70.0) {
            return new RiskAnalysis(RiskType.SUBJECT_GRADE, RiskLevel.WARNING);
        }

        return null; // 정상
    }
}


/**
    // ★ 새로 추가
    // 성적/출결 기반으로 RiskType 결정
    public RiskType decideRiskType(StuSub stuSub, StuSubDetail detail) {
        boolean attendanceBad = detail.getAbsent() != null && detail.getAbsent() >= 5;
        boolean gradeBad = "F".equalsIgnoreCase(detail.getGrade());

        if (attendanceBad && gradeBad) return RiskType.BOTH;
        if (attendanceBad) return RiskType.ATTENDANCE;
        if (gradeBad) return RiskType.SUBJECT_GRADE;
        return null; // 위험 아님
    }


    // AI 요청 DTO 만들어 주는 헬퍼 (기존 evaluateAndAnalyzeRisk 분리 용도)
    public AiRiskAnalysisRequest buildRequest(StuSub stuSub, StuSubDetail detail) {
        // 네가 이미 쓰던 필드 그대로 매핑
        AiRiskAnalysisRequest req = new AiRiskAnalysisRequest();
        req.setStudentName(stuSub.getStudent().getName());
        req.setSubjectName(stuSub.getSubject().getName());
        req.setAbsent(detail.getAbsent());
        req.setLateness(detail.getLateness());
        req.setConvertedMark(detail.getConvertedMark());
        req.setGrade(detail.getGrade());
        // semesterGpa 등 필요한 필드 추가
        // riskType, riskLevel도 여기서 계산해서 셋팅
        return req;
    }

    // student + subject + riskType 기준 최신 1개 유지
    @Transactional
    public void upsertDropoutRisk(StuSub stuSub,
                                  RiskType riskType,
                                  RiskLevel riskLevel,
                                  String lastAiInput,
                                  AiRiskAnalysisResult aiResult) {
        if (riskType == null) {
            return; // 위험 아님 → 저장 안 함
        }

        DropoutRisk risk = dropoutRiskRepository
                .findByStuSubAndRiskType(stuSub, riskType)
                .orElseGet(DropoutRisk::new);

        risk.setStuSub(stuSub);
        risk.setRiskType(riskType);
        risk.setRiskLevel(riskLevel);
        risk.setLastAiInput(lastAiInput);
        // 너 엔티티에 있는 편의메서드 사용
        risk.updateFromAiResult(aiResult);
        dropoutRiskRepository.save(risk);
    }
}
*/






/**
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
*/


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

