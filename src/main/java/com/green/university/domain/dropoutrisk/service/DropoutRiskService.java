package com.green.university.domain.dropoutrisk.service;

import com.green.university.domain.counseling.entity.ApprovalState;
import com.green.university.domain.counseling.entity.CounselingReserve;
import com.green.university.domain.counseling.entity.ReserveRequester;
import com.green.university.domain.counseling.repository.CounselingReserveRepository;
import com.green.university.domain.dropoutrisk.dto.DropoutRiskResponseDto;
import com.green.university.domain.dropoutrisk.dto.DropoutStudentRiskRowDto;
import com.green.university.domain.dropoutrisk.entity.DropoutRisk;
import com.green.university.domain.dropoutrisk.entity.RiskLevel;
import com.green.university.domain.dropoutrisk.entity.RiskStatus;
import com.green.university.domain.dropoutrisk.entity.RiskType;
import com.green.university.domain.dropoutrisk.respository.DropoutRiskRepository;
import com.green.university.domain.grade.service.GradeService;
import com.green.university.domain.professor.entity.Professor;
import com.green.university.domain.professor.repository.ProfessorRepository;
import com.green.university.domain.subject.entity.StuSub;
import com.green.university.domain.subject.entity.StuSubDetail;
import com.green.university.domain.subject.entity.Subject;
import com.green.university.domain.subject.repository.SubjectRepository;
import com.green.university.global.exception.CustomRestfullException;
import com.green.university.infra.ai.dto.AiRiskAnalysisRequest;
import com.green.university.infra.ai.dto.AiRiskAnalysisResult;
import com.green.university.infra.ai.service.AiAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    private final ProfessorRepository professorRepository; // 학과 교수들에게 보여주기위해 교수의 학과ID 조회용

    // ================= 조회 로직 추가 =================
    // 해당 교수의 강의 중 + 상담 미완료, 완료된 학생을 나눠서 보여주기 + 검색 (과목, 위험레벨)
    @Transactional(readOnly = true)
    public Map<String, List<DropoutRiskResponseDto>> getRisksByStatus(Long subjectId, RiskLevel riskLevel, Long professorId) {

        // 과목 담당교수 기준으로 위험학생 조회 (subjectId/riskLevel 필터 가능)
        List<DropoutRisk> dropoutRisks = loadDropoutRisksBySubjectProfessor(professorId, subjectId, riskLevel);

        Map<String, List<DropoutRiskResponseDto>> map = new HashMap<>();

        map.put("pending", dropoutRisks.stream().filter(
                        r -> r.getStatus().equals(RiskStatus.DETECTED) || r.getStatus().equals(RiskStatus.CONSULT_REQ))
                .map(r -> {
                    // 위험과목 상담 동기화: DropoutRisk에 연결된 상담예약 기준으로 상태 계산
                    // 내 통합상담을 내 과목 위험row에 반영해야 하므로 professorId도 같이 넘긴다.
                    String consultState = computeConsultState(r, professorId);
                    return DropoutRiskResponseDto.fromEntity(r, consultState);
                })
                .collect(Collectors.toList()));

        map.put("resolved", dropoutRisks.stream().filter(
                        r -> r.getStatus().equals(RiskStatus.RESOLVED))
                .map(r -> {
                    String consultState = computeConsultState(r, professorId); // resolved도 같이 내려주고 싶으면 유지
                    return DropoutRiskResponseDto.fromEntity(r, consultState);
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
            // 해당 교수의 모든 과목 위험학생
            return dropoutRiskRepository.findByStuSub_Subject_Professor_Id(professorId);
        } else if (subjectId != null && riskLevel == null) {
            // 특정 과목 위험학생 (이미 위에서 교수 검증 완료)
            return dropoutRiskRepository.findByStuSub_Subject_Id(subjectId);
        } else if (subjectId == null) {
            // 해당 교수의 모든 과목 중 특정 위험레벨
            return dropoutRiskRepository.findByStuSub_Subject_Professor_IdAndRiskLevel(professorId, riskLevel);
        } else {
            // 특정 과목 + 특정 위험레벨 (이미 위에서 교수 검증 완료)
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

    // 학과 기준 위험학생 조회 (subjectId/riskLevel 필터)
    // (통합(탈락위험) 학생 목록에서 사용)
    private List<DropoutRisk> loadDropoutRisksByDepartment(Long departmentId, Long subjectId, RiskLevel riskLevel) {

        // subjectId가 들어오면, 학과 과목인지 검증
        if (subjectId != null) {
            Subject subject = subjectRepository.findById(subjectId).orElseThrow(
                    () -> new CustomRestfullException("해당 과목을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
            );

            // ⚠️ Subject에 department가 없을 수 있으므로 professor.department 기준으로 검증 (필드명 다르면 여기만 수정)
            if (subject.getProfessor() == null || subject.getProfessor().getDepartment() == null ||
                    !subject.getProfessor().getDepartment().getId().equals(departmentId)) {
                throw new CustomRestfullException("본인 학과 과목만 조회 할 수 있습니다.", HttpStatus.FORBIDDEN);
            }
        }

        // 조건에 따라 조회
        if (subjectId == null && riskLevel == null) {
            // 해당 학과 학생들의 모든 위험학생
            return dropoutRiskRepository.findByStuSub_Student_Department_Id(departmentId);
        } else if (subjectId != null && riskLevel == null) {
            // 해당 학과 + 특정 과목
            return dropoutRiskRepository.findByStuSub_Student_Department_IdAndStuSub_Subject_Id(departmentId, subjectId);
        } else if (subjectId == null) {
            // 해당 학과 + 특정 위험레벨
            return dropoutRiskRepository.findByStuSub_Student_Department_IdAndRiskLevel(departmentId, riskLevel);
        } else {
            // 해당 학과 + 특정 과목 + 특정 위험레벨
            return dropoutRiskRepository.findByStuSub_Student_Department_IdAndStuSub_Subject_IdAndRiskLevel(
                    departmentId,
                    subjectId,
                    riskLevel
            );
        }
    }

    // 위험과목 상담 동기화 전용 consultState 계산
    // DropoutRisk에 연결된 CounselingReserve 최신 1건을 기준으로 UI 상태를 계산한다.
    // 일반 상담(위험과목 아닌 상담)은 consultState에 영향을 주지 않음.
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
                    };
                })
                .orElse(null);
    }


    // =============== AI 분석 + 저장 로직 ===============
    // 출결 및 성적 기반 위험도 계산 + AI 분석 요청 + DB 저장
    @Transactional
    public void evaluateAndAnalyzeRisk(StuSub stuSub, StuSubDetail detail) {
        log.info("🔍 AI 분석 시작 - 학생: {}, 과목: {}",
                stuSub.getStudent().getName(),
                stuSub.getSubject().getName());

        // 1. 위험도 계산 (비즈니스 로직)
        RiskAnalysis analysis = calculateRisk(detail);

        // 위험이 없으면(NULL) 기존 리스크가 있다면 해결(RESOLVED) 처리 후 종료하거나, 그냥 종료
        if (analysis == null) {
            log.info("⚠ 위험 없음 - 분석 종료 (학생: {})", stuSub.getStudent().getName());
            // 필요하다면 여기서 기존 DETECTED 상태인 리스크를 찾아서 RESOLVED로 바꾸는 로직 추가 가능
            return;
        }

        log.info("📊 위험 감지됨 - Type: {}, Level: {}", analysis.type, analysis.level);

        // 2. AI 분석 요청 데이터 생성
        // 직전 학기 평점 가져오기 (없으면 0.0)
        double prevGpa = 0.0;
        try {
            // GradeService readMyGradeByStudentId는 금학기 누계 성적임..
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

        // 3. AI 서비스 호출 (오래 걸릴 수 있음 - 동기 + 블로킹 방식)
        log.info("🤖 AI 서비스 호출 중...");
        AiRiskAnalysisResult aiResult = aiAnalysisService.analyzeRisk(request);
        log.info("✅ AI 응답 받음: {}", aiResult.getSummary());

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
        log.info("💾 DB 저장 완료 - riskId: {}, studentName: {}, type: {}", risk.getId(), stuSub.getStudent().getName(), analysis.type);
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
        String grade = detail.getLetterGrade();
        boolean attendanceDanger = totalAbsent >= 4;
        boolean gradeDanger = "F".equalsIgnoreCase(grade);

        // 1. 위험 레벨: DANGER (결석 4회 이상 OR F학점)
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

    // 학생 통합(탈락 위험) 리스트
    // 학과 교수들" 화면에서 학생 단위로 위험을 합쳐서 보여주기
    // - rule: 위험 과목이 여러 개면 overall DANGER/WARNING 상승
    // - 담당(지정) 교수: 학생 기준 최신 "교수요청" 상담예약(Reserve)에서 계산
    @Transactional(readOnly = true)
    public List<DropoutStudentRiskRowDto> getStudentOverallRisks(Long subjectId, RiskLevel riskLevel, Long professorId) {

        // 교수의 소속 학과 id
        Long departmentId = getProfessorDepartmentId(professorId);

        // 학과 기준으로 위험학생 조회
        List<DropoutRisk> dropoutRisks = loadDropoutRisksByDepartment(departmentId, subjectId, riskLevel);

        // 학생 통합 위험은 보통 미완료(=현재 개입 필요) 기준
        // DETECTED/CONSULT_REQ만 모아서 학생 단위로 합산함
        Map<Long, List<DropoutRisk>> groupedByStudent = dropoutRisks.stream()
                .filter(r -> r.getStatus() == RiskStatus.DETECTED || r.getStatus() == RiskStatus.CONSULT_REQ)
                .filter(r -> r.getStuSub() != null && r.getStuSub().getStudent() != null)
                .collect(Collectors.groupingBy(r -> r.getStuSub().getStudent().getId()));

        List<DropoutStudentRiskRowDto> students = groupedByStudent.entrySet().stream()
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

                    // 담당 교수(지정) 표시: 학생 기준 가장 최근 교수요청
                    AssignedProfessor assigned = computeAssignedProfessor(studentId);

                    // ✅ [MOD] 내(로그인 교수) 기준 통합상담 상태 (다른 교수 통합상담과 분리)
                    ApprovalState myOverall = computeMyOverallApprovalState(studentId, professorId);

                    return DropoutStudentRiskRowDto.builder()
                            .studentId(studentId)
                            .studentName(studentName)
                            .dangerCount(dangerCount)
                            .warningCount(warningCount)
                            .overallLevel(overallLevel)
                            .reason("DANGER " + dangerCount + ", WARNING " + warningCount)
                            .updatedAt(latest)
                            .assignedProfessorId(assigned.professorId)
                            .assignedProfessorName(assigned.professorName)
                            .assignedAt(assigned.assignedAt)
                            .assignedApprovalState(assigned.approvalState)
                            .myOverallApprovalState(myOverall)
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

        return students;
    }

    // 학생 통합 위험 등급 계산 (최소 규칙)
    // - DANGER 과목이 2개 이상 -> DANGER
    // - DANGER 1개 + WARNING 1개 이상 -> DANGER
    // - DANGER 1개 -> WARNING
    // - WARNING 2개 이상 -> WARNING
    // - 그 외 -> NORMAL
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

    // 내(로그인 교수) 기준 통합상담 상태 조회
    // - 통합상담은 subject=null && dropoutRisk=null && requester=PROFESSOR 로 판별한다.
    private ApprovalState computeMyOverallApprovalState(Long studentId, Long professorId) {
        Optional<CounselingReserve> myOverallOpt =
                counselingReserveRepository.findTop1OverallByStudentAndProfessor(studentId, professorId);

        return myOverallOpt.map(CounselingReserve::getApprovalState).orElse(null);
    }

    // 학생 기준 가장 최근에 교수 요청 상담예약을 만든 교수를 담당으로 표시
    // 통합상담/교수요청 기준 담당교수 계산 + approvalState(처리중 표시용)까지 같이 내려줌
    private AssignedProfessor computeAssignedProfessor(Long studentId) {

        // 1) 통합(OVERALL) 상담이 있으면 그걸 우선
        // 통합상담 판별을 "dropoutRisk null"이 아니라 "subject null && dropoutRisk null"로 고정한다.
        Optional<CounselingReserve> overallOpt =
                counselingReserveRepository.findTop1OverallByStudent(studentId);

        if (overallOpt.isPresent()) {
            CounselingReserve latest = overallOpt.get();
            return buildAssignedFromReserve(latest);
        }

        // 2) 통합이 없으면 기존 방식: 학생 기준 가장 최근 교수요청 1건
        Optional<CounselingReserve> latestOpt = counselingReserveRepository
                .findTop1ByStudent_IdAndRequesterOrderByIdDesc(
                        studentId,
                        com.green.university.domain.counseling.entity.ReserveRequester.PROFESSOR
                );

        if (latestOpt.isEmpty()) {
            return new AssignedProfessor(null, null, null, null);
        }

        return buildAssignedFromReserve(latestOpt.get());
    }

    // reserve -> AssignedProfessor 변환 (중복 제거)
    private AssignedProfessor buildAssignedFromReserve(CounselingReserve reserve) {
        if (reserve == null) return new AssignedProfessor(null, null, null, null);

        if (reserve.getCounselingSchedule() == null || reserve.getCounselingSchedule().getProfessor() == null) {
            return new AssignedProfessor(null, null, null, reserve.getApprovalState());
        }

        Long pid = reserve.getCounselingSchedule().getProfessor().getId();
        String pname = reserve.getCounselingSchedule().getProfessor().getName();

        LocalDate date = reserve.getCounselingSchedule().getCounselingDate();
        LocalTime time = toLocalTime(reserve.getCounselingSchedule().getStartTime());

        LocalDateTime at = null;
        if (date != null && time != null) {
            at = LocalDateTime.of(date, time);
        }

        return new AssignedProfessor(pid, pname, at, reserve.getApprovalState());
    }

    // startTime(Long)을 LocalTime으로 변환
    // - 주로 HHmm(예: 900, 1500) 형태를 가정
    // - 혹시 15 처럼 시간만 저장해도 대응(정시)
    private LocalTime toLocalTime(Long v) {
        if (v == null) return null;

        // 15, 9 처럼 "시간만" 들어오면 정시로 처리
        if (v >= 0 && v <= 23) {
            return LocalTime.of(v.intValue(), 0);
        }

        // 900, 1500 같은 HHmm
        String s = String.format("%04d", v);
        int hh = Integer.parseInt(s.substring(0, 2));
        int mm = Integer.parseInt(s.substring(2, 4));

        if (hh < 0 || hh > 23 || mm < 0 || mm > 59) return null;
        return LocalTime.of(hh, mm);
    }

    // 내부용 record (DTO에 바로 넣기 전 중간값)
    private record AssignedProfessor(Long professorId, String professorName, LocalDateTime assignedAt, ApprovalState approvalState) {}

    // 학과 기준: 선택 학생의 위험과목 전체
    @Transactional(readOnly = true)
    public List<DropoutRiskResponseDto> getDepartmentPendingRisks(Long studentId, RiskLevel level, Long professorId) {

        // 교수의 학과를 얻어서 같은 학과 학생만 보이게
        Long deptId = professorRepository.findById(professorId)
                .orElseThrow(() -> new CustomRestfullException("교수 정보 없음", HttpStatus.BAD_REQUEST))
                .getDepartment().getId();

        // 같은 학과의 위험 데이터 중에서, studentId만 필터
        List<DropoutRisk> list;

        if (level == null) {
            list = dropoutRiskRepository
                    .findByStuSub_Student_Department_IdAndStuSub_Student_Id(deptId, studentId);
        } else {
            list = dropoutRiskRepository
                    .findByStuSub_Student_Department_IdAndStuSub_Student_IdAndRiskLevel(deptId, studentId, level);
        }

        return list.stream()
                .map(r -> DropoutRiskResponseDto.fromEntity(r, computeConsultState(r, professorId)) /* + consultState 필요하면 여기서 계산 */)
                .toList();
    }
}
