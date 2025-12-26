package com.green.university.domain.dropoutrisk.service;

import com.green.university.domain.counseling.entity.ApprovalState;
import com.green.university.domain.counseling.entity.CounselingReserve;
import com.green.university.domain.counseling.repository.CounselingReserveRepository;
import com.green.university.domain.dropoutrisk.dto.DropoutRiskResponseDto;
import com.green.university.domain.dropoutrisk.dto.DropoutStudentRiskRowDto;
import com.green.university.domain.dropoutrisk.entity.*;
import com.green.university.domain.dropoutrisk.respository.DropoutRiskRepository;
import com.green.university.domain.grade.service.GradeService;
import com.green.university.domain.professor.entity.Professor;
import com.green.university.domain.professor.repository.ProfessorRepository;
import com.green.university.domain.subject.entity.StuSub;
import com.green.university.domain.subject.entity.StuSubDetail;
import com.green.university.domain.subject.entity.Subject;
import com.green.university.domain.subject.repository.SubjectRepository;
import com.green.university.global.exception.CustomRestfullException;
import com.green.university.global.utils.TermUtil;
import com.green.university.infra.ai.dto.AiRiskAnalysisRequest;
import com.green.university.infra.ai.dto.AiRiskAnalysisResult;
import com.green.university.infra.ai.service.AiAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
// ☎️ 출결 및 성적 기반 위험도 계산 + AI 분석 요청 + DB 저장 + 조회 (위험 학생 목록 가져오기)
public class DropoutRiskService {

    private final DropoutRiskRepository dropoutRiskRepository;
    private final AiAnalysisService aiAnalysisService; // AI API 호출 서비스
    private final GradeService gradeService;
    private final SubjectRepository subjectRepository;

    private final CounselingReserveRepository counselingReserveRepository; // consultState 계산용
    private final ProfessorRepository professorRepository; // 학과 교수들의 학과ID 조회용

    // 교수 위험학생 페이지: 한번에 내려주는 응답 구성
    @Transactional(readOnly = true)
    public Map<String, Object> getProfessorRiskOverview(Long subjectId, RiskLevel riskLevel, Long professorId) {

        // 1) 내 담당 과목 위험학생 (pending/resolved)
        Map<String, List<DropoutRiskResponseDto>> myGrouped = getRisksByStatus(subjectId, riskLevel, professorId);

        // 2) 우리학과 위험학생 (학생 단위 집계)
        List<DropoutStudentRiskRowDto> deptStudents = getDepartmentStudentOverallRisks(riskLevel, professorId);

        // 3) 과목 옵션(내 과목)
        List<Map<String, Object>> subjectOptions =
                subjectRepository.findByProfessor_IdAndSubYearAndSemester(
                                professorId, TermUtil.currentYear(), TermUtil.currentSemester()
                        ).stream()
                        .map(s -> {
                            Map<String, Object> m = new LinkedHashMap<>();
                            m.put("value", s.getId());
                            m.put("label", s.getName());
                            return m;
                        })
                        .collect(Collectors.toList());

        return Map.of(
                "myPending", myGrouped.get("pending"),
                "myResolved", myGrouped.get("resolved"),
                "departmentStudents", deptStudents,
                "subjectOptions", subjectOptions
        );
    }

    // 해당 교수의 강의 중 + 상담 미완료, 완료된 학생을 나눠서 보여주기 + 검색 (과목, 위험레벨)
    @Transactional(readOnly = true)
    public Map<String, List<DropoutRiskResponseDto>> getRisksByStatus(Long subjectId, RiskLevel riskLevel, Long professorId) {

        // 과목 담당교수 기준으로 위험학생 조회 (subjectId/riskLevel 필터 가능)
        List<DropoutRisk> dropoutRisks = loadDropoutRisksBySubjectProfessor(professorId, subjectId, riskLevel);

        Map<String, List<DropoutRiskResponseDto>> map = new HashMap<>();

        map.put("pending", dropoutRisks.stream()
                .filter(r -> r.getStatus() == RiskStatus.DETECTED || r.getStatus() == RiskStatus.CONSULT_REQ)
                .map(r -> {
                    // 위험과목 상담 동기화: DropoutRisk에 연결된 상담예약 기준으로 상태 계산
                    String consultState = computeConsultState(r);
                    return DropoutRiskResponseDto.fromEntity(r, consultState, true); // ✅ [MOD] 내 과목은 true
                })
                .collect(Collectors.toList()));

        map.put("resolved", dropoutRisks.stream()
                .filter(r -> r.getStatus() == RiskStatus.RESOLVED)
                .map(r -> {
                    String consultState = computeConsultState(r);
                    return DropoutRiskResponseDto.fromEntity(r, consultState, true); // ✅ [MOD]
                })
                .collect(Collectors.toList()));

        return map;
    }

    // 과목 담당교수 기준 위험학생 조회 (subjectId/riskLevel 필터)
    private List<DropoutRisk> loadDropoutRisksBySubjectProfessor(Long professorId, Long subjectId, RiskLevel riskLevel) {

        // subjectId가 들어오면, "내 과목인지" 검증
        if (subjectId != null) {
            Subject subject = subjectRepository.findById(subjectId).orElseThrow(
                    () -> new CustomRestfullException("해당 과목을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
            );

            if (subject.getProfessor() == null || subject.getProfessor().getId() == null ||
                    !subject.getProfessor().getId().equals(professorId)) {
                throw new CustomRestfullException("본인 강의만 조회 할 수 있습니다.", HttpStatus.FORBIDDEN);
            }
        }

        // 조건에 따라 조회
        if (subjectId == null && riskLevel == null) {
            return dropoutRiskRepository.findByStuSub_Subject_Professor_Id(professorId);
        } else if (subjectId != null && riskLevel == null) {
            return dropoutRiskRepository.findByStuSub_Subject_Id(subjectId);
        } else if (subjectId == null) {
            return dropoutRiskRepository.findByStuSub_Subject_Professor_IdAndRiskLevel(professorId, riskLevel);
        } else {
            return dropoutRiskRepository.findByStuSub_Subject_IdAndRiskLevel(subjectId, riskLevel);
        }
    }

    // 교수의 소속 학과 id 가져오기
    private Long getProfessorDepartmentId(Long professorId) {
        Professor professor = professorRepository.findById(professorId).orElseThrow(
                () -> new CustomRestfullException("교수 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        );

        // ⚠️ 교수 엔티티에서 학과 필드명이 다르면 여기만 수정
        if (professor.getDepartment() == null) {
            throw new CustomRestfullException("교수의 소속 학과 정보가 없습니다.", HttpStatus.BAD_REQUEST);
        }

        return professor.getDepartment().getId();
    }

    // 우리학과 위험학생(학생 단위 집계)
    @Transactional(readOnly = true)
    public List<DropoutStudentRiskRowDto> getDepartmentStudentOverallRisks(RiskLevel riskLevel, Long professorId) {

        Long deptId = getProfessorDepartmentId(professorId);

        // 학과 학생들의 위험 데이터 로드
        List<DropoutRisk> all = (riskLevel == null)
                ? dropoutRiskRepository.findByStuSub_Student_Department_Id(deptId)
                : dropoutRiskRepository.findByStuSub_Student_Department_IdAndRiskLevel(deptId, riskLevel);

        // 학생 단위 집계는 "현재 개입 필요" 기준만
        Map<Long, List<DropoutRisk>> grouped = all.stream()
                .filter(r -> r.getStatus() == RiskStatus.DETECTED || r.getStatus() == RiskStatus.CONSULT_REQ)
                .filter(r -> r.getStuSub() != null && r.getStuSub().getStudent() != null)
                .collect(Collectors.groupingBy(r -> r.getStuSub().getStudent().getId()));

        return grouped.entrySet().stream()
                .map(e -> {
                    Long studentId = e.getKey();
                    List<DropoutRisk> rs = e.getValue();
                    String studentName = rs.get(0).getStuSub().getStudent().getName();

                    int dangerCount = (int) rs.stream().filter(r -> r.getRiskLevel() == RiskLevel.DANGER).count();
                    int warningCount = (int) rs.stream().filter(r -> r.getRiskLevel() == RiskLevel.WARNING).count();

                    String overallLevel = computeOverallLevel(dangerCount, warningCount);

                    LocalDateTime latest = rs.stream()
                            .map(DropoutRisk::getUpdatedAt)
                            .filter(Objects::nonNull)
                            .max(Comparator.naturalOrder())
                            .orElse(null);

                    return DropoutStudentRiskRowDto.builder()
                            .studentId(studentId)
                            .studentName(studentName)
                            .dangerCount(dangerCount)
                            .warningCount(warningCount)
                            .overallLevel(overallLevel)
                            .reason("DANGER " + dangerCount + ", WARNING " + warningCount)
                            .updatedAt(latest)

                            // ✅ [MOD] 통합신청 제거 → null로 내려감
                            .assignedProfessorId(null)
                            .assignedProfessorName(null)
                            .assignedAt(null)
                            .build();
                })
                // 위험도 높은 학생 먼저 + 최신 업데이트 먼저
                .sorted((a, b) -> {
                    int ra = overallRank(a.getOverallLevel());
                    int rb = overallRank(b.getOverallLevel());
                    if (ra != rb) return Integer.compare(rb, ra);

                    LocalDateTime ta = a.getUpdatedAt();
                    LocalDateTime tb = b.getUpdatedAt();
                    if (ta == null && tb == null) return 0;
                    if (ta == null) return 1;
                    if (tb == null) return -1;
                    return tb.compareTo(ta);
                })
                .toList();
    }

    // 학생 통합 위험 등급 계산 (최소 규칙)
    private String computeOverallLevel(int dangerCount, int warningCount) {
        if (dangerCount >= 2) return "DANGER";
        if (dangerCount >= 1 && warningCount >= 1) return "DANGER";
        if (dangerCount >= 1) return "WARNING";
        if (warningCount >= 2) return "WARNING";
        return "NORMAL";
    }

    private int overallRank(String lvl) {
        if ("DANGER".equals(lvl)) return 3;
        if ("WARNING".equals(lvl)) return 2;
        return 1;
    }

    // 위험과목 상담 동기화 전용 consultState 계산
    // DropoutRisk에 연결된 CounselingReserve 최신 1건을 기준으로 UI 상태를 계산한다.
    private String computeConsultState(DropoutRisk risk) {
        if (risk == null || risk.getId() == null) return null;

        return counselingReserveRepository.findTop1ByDropoutRisk_IdOrderByIdDesc(risk.getId())
                .map(r -> {
                    if (r.getApprovalState() == null) return null;
                    return switch (r.getApprovalState()) {
                        case REQUESTED -> "CONSULT_REQ";
                        case APPROVED  -> "CONSULT_APPROVED";
                        case REJECTED  -> "CONSULT_REJECTED";
                        case CANCELED  -> "CONSULT_CANCELED";
                        case FINISHED  -> "CONSULT_FINISHED";
                        case NO_SHOW -> "CONSULT_NO_SHOW";
                    };
                })
                .orElse(null);
    }

    // 학과 기준: 선택 학생의 위험과목 전체
    @Transactional(readOnly = true)
    public List<DropoutRiskResponseDto> getDepartmentPendingRisks(Long studentId, RiskLevel level, Long professorId) {

        Long deptId = professorRepository.findById(professorId)
                .orElseThrow(() -> new CustomRestfullException("교수 정보 없음", HttpStatus.BAD_REQUEST))
                .getDepartment().getId();

        List<DropoutRisk> list = (level == null)
                ? dropoutRiskRepository.findByStuSub_Student_Department_IdAndStuSub_Student_Id(deptId, studentId)
                : dropoutRiskRepository.findByStuSub_Student_Department_IdAndStuSub_Student_IdAndRiskLevel(deptId, studentId, level);

        // 내 과목 여부(mySubject)
        return list.stream()
                .map(r -> {
                    boolean mySubject =
                            r.getStuSub() != null &&
                                    r.getStuSub().getSubject() != null &&
                                    r.getStuSub().getSubject().getProfessor() != null &&
                                    Objects.equals(r.getStuSub().getSubject().getProfessor().getId(), professorId);

                    return DropoutRiskResponseDto.fromEntity(r, computeConsultState(r), mySubject);
                })
                .toList();
    }

    // =============== AI 분석 + 저장 로직(기존 유지) ===============
    @Transactional
    public void evaluateAndAnalyzeRisk(StuSub stuSub, StuSubDetail detail) {
        log.info("🔍 AI 분석 시작 - 학생: {}, 과목: {}",
                stuSub.getStudent().getName(),
                stuSub.getSubject().getName());

        RiskAnalysis analysis = calculateRisk(detail);

        if (analysis == null) {
            log.info("⚠ 위험 없음 - 분석 종료 (학생: {})", stuSub.getStudent().getName());
            return;
        }

        log.info("📊 위험 감지됨 - Type: {}, Level: {}", analysis.type, analysis.level);

        double prevGpa = 0.0;
        try {
            var myGrade = gradeService.readMyGradeByStudentId(stuSub.getStudent().getId());
            if (myGrade != null) prevGpa = myGrade.getAverage();
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
                .letterGrade(detail.getLetterGrade())
                .semesterGpa(prevGpa)
                .riskType(analysis.type)
                .riskLevel(analysis.level)
                .build();

        log.info("🤖 AI 서비스 호출 중...");
        AiRiskAnalysisResult aiResult = aiAnalysisService.analyzeRisk(request);
        log.info("✅ AI 응답 받음: {}", aiResult.getSummary());

        DropoutRisk risk = dropoutRiskRepository.findByStuSubAndRiskType(stuSub, analysis.type)
                .orElseGet(() -> DropoutRisk.builder()
                        .stuSub(stuSub)
                        .riskType(analysis.type)
                        .build());

        risk.setRiskLevel(analysis.level);
        risk.setStatus(RiskStatus.DETECTED);
        risk.setLastAiInput(request.toString());

        risk.setAiSummary(aiResult.getSummary());
        risk.setAiRecommendation(aiResult.getProfessorGuide());
        risk.setAiStudentMessage(aiResult.getStudentMessage());
        if (aiResult.getReasonTags() != null) {
            risk.setAiReasonTags(String.join(",", aiResult.getReasonTags()));
        }

        dropoutRiskRepository.save(risk);
        log.info("💾 DB 저장 완료 - riskId: {}, studentName: {}, type: {}", risk.getId(), stuSub.getStudent().getName(), analysis.type);
    }

    // 룸코드 + 예약승인된 예약의 상태 RESOLVED, FINISHED로 변경하기
    @Transactional
    public void completeCounseling(String roomCode) {
        CounselingReserve reserve = counselingReserveRepository.findByRoomCodeAndApprovalState(roomCode, ApprovalState.APPROVED);
        if (reserve == null) {
            throw new CustomRestfullException("승인된 상담 예약을 찾을 수 없습니다. (roomCode: " + roomCode + ")", HttpStatus.NOT_FOUND);
        }
        DropoutRisk dropoutRisk = reserve.getDropoutRisk();
        if (dropoutRisk != null) {
            dropoutRisk.setStatus(RiskStatus.RESOLVED);
            dropoutRiskRepository.save(dropoutRisk);
        }
        reserve.setApprovalState(ApprovalState.FINISHED);
    }

    // ================= 헬퍼 메서드 =================
    private record RiskAnalysis(RiskType type, RiskLevel level) {}

    private RiskAnalysis calculateRisk(StuSubDetail detail) {
        long absent = detail.getAbsent() == null ? 0 : detail.getAbsent();
        long lateness = detail.getLateness() == null ? 0 : detail.getLateness();
        long totalAbsent = absent + (lateness / 3);

        String grade = detail.getLetterGrade();
        boolean attendanceDanger = totalAbsent >= 4;
        boolean gradeDanger = "F".equalsIgnoreCase(grade);

        if (attendanceDanger && gradeDanger) {
            return new RiskAnalysis(RiskType.BOTH, RiskLevel.DANGER);
        }
        if (attendanceDanger) {
            return new RiskAnalysis(RiskType.ATTENDANCE, RiskLevel.DANGER);
        }
        if (gradeDanger) {
            return new RiskAnalysis(RiskType.SUBJECT_GRADE, RiskLevel.DANGER);
        }

        if (totalAbsent >= 3) {
            return new RiskAnalysis(RiskType.ATTENDANCE, RiskLevel.WARNING);
        }

        if (detail.getConvertedMark() != null && detail.getConvertedMark() < 70.0) {
            return new RiskAnalysis(RiskType.SUBJECT_GRADE, RiskLevel.WARNING);
        }

        return null;
    }
}
