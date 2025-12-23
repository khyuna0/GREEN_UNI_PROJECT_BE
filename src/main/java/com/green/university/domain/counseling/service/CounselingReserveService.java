package com.green.university.domain.counseling.service;

import com.green.university.domain.counseling.dto.CounselPreReserveDto;
import com.green.university.domain.counseling.dto.CounselingProfessorRequestDto;
import com.green.university.domain.counseling.dto.CounselingReserveDto;
import com.green.university.domain.counseling.dto.CounselingStudentRequestDto;
import com.green.university.domain.counseling.entity.ApprovalState;
import com.green.university.domain.counseling.entity.CounselingReserve;
import com.green.university.domain.counseling.entity.CounselingSchedule;
import com.green.university.domain.counseling.entity.ReserveRequester;
import com.green.university.domain.counseling.repository.CounselingReserveRepository;
import com.green.university.domain.counseling.repository.CounselingScheduleRepository;
import com.green.university.domain.dropoutrisk.entity.DropoutRisk;
import com.green.university.domain.dropoutrisk.entity.RiskStatus;
import com.green.university.domain.student.entity.Student;
import com.green.university.domain.student.repository.StudentRepository;
import com.green.university.domain.subject.entity.StuSub;
import com.green.university.domain.subject.entity.Subject;
import com.green.university.domain.subject.repository.StuSubRepository;
import com.green.university.domain.subject.repository.SubjectRepository;
import com.green.university.global.exception.CustomRestfullException;
import com.green.university.domain.dropoutrisk.respository.DropoutRiskRepository;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class CounselingReserveService {

    @Autowired
    private CounselingReserveRepository counselingReserveRepository;

    @Autowired
    private CounselingScheduleRepository counselingScheduleRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private StuSubRepository stuSubRepository;

    @Autowired
    private DropoutRiskRepository dropoutRiskRepository;

    // 학생 상담 신청
    // 하나의 상담 일정에 여러 학생이 동시에 신청 가능
    // 최초 상태는 REQUESTED
    public void requestReserve(CounselingStudentRequestDto dto, Long studentId) {

        // 같은 과목으로 "무한 신청" 막기 (REQUESTED 기준, requester 구분)
        boolean hasMyRequested =
                counselingReserveRepository.existsByStudent_IdAndSubject_IdAndApprovalStateAndRequester(
                        studentId,
                        dto.getSubjectId(),
                        ApprovalState.REQUESTED,
                        ReserveRequester.STUDENT
                );

        if (hasMyRequested) {
            throw new CustomRestfullException(
                    "이미 신청된 상담 요청이 있습니다",
                    HttpStatus.BAD_REQUEST
            );
        }

        boolean hasProfessorRequestedToMe =
                counselingReserveRepository.existsByStudent_IdAndSubject_IdAndApprovalStateAndRequester(
                        studentId,
                        dto.getSubjectId(),
                        ApprovalState.REQUESTED,
                        ReserveRequester.PROFESSOR
                );

        if (hasProfessorRequestedToMe) {
            throw new CustomRestfullException(
                    "교수에게 요청온 상담 내역이 있습니다",
                    HttpStatus.BAD_REQUEST
            );
        }

        // 같은 슬롯 중복 신청은 REQUESTED/APPROVED만 막고, REJECTED는 재신청 가능
        boolean alreadyReserved =
                counselingReserveRepository.existsByStudent_IdAndCounselingSchedule_IdAndApprovalStateIn(
                        studentId,
                        dto.getCounselingScheduleId(),
                        List.of(ApprovalState.REQUESTED, ApprovalState.APPROVED)
                );

        if (alreadyReserved) {
            throw new CustomRestfullException(
                    "이미 해당 상담 일정에 신청한 내역이 있습니다.",
                    HttpStatus.BAD_REQUEST
            );
        }

        // 학생 조회
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() ->
                        new CustomRestfullException("학생을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
                );

        // 과목 조회
        Subject subject = subjectRepository.findById(dto.getSubjectId())
                .orElseThrow(() ->
                        new CustomRestfullException("과목을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
                );

        // 상담 일정 조회
        CounselingSchedule schedule = counselingScheduleRepository.findById(dto.getCounselingScheduleId())
                .orElseThrow(() ->
                        new CustomRestfullException("상담 일정을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
                );

        // 예약 엔티티 생성
        CounselingReserve reserve = new CounselingReserve();
        reserve.setStudent(student);
        reserve.setSubject(subject);
        reserve.setCounselingSchedule(schedule);
        reserve.setReason(dto.getReason());
        reserve.setApprovalState(ApprovalState.REQUESTED);
        reserve.setRequester(ReserveRequester.STUDENT); // 학생이 신청

        // 위험학생이면 dropoutRisk 연결 (학생+과목의 StuSub 기반)
        attachDropoutRiskIfExists(reserve, studentId, dto.getSubjectId());

        // 저장
        counselingReserveRepository.save(reserve);
    }

    // 교수 승인 / 반려 처리
    // 승인 시 같은 일정의 다른 신청은 전부 반려
    // 승인 시 방 코드 생성
    // 위험 학생이면 RiskStatus를 CONSULT_REQ 로 변경
    public void decideReserve(Long reserveId, String decision) {

        // 예약 조회
        CounselingReserve reserve = counselingReserveRepository.findById(reserveId)
                .orElseThrow(() ->
                        new CustomRestfullException("상담 예약을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
                );

        // decision 문자열 방어 (프론트가 "반려/승인" 또는 "REJECTED/APPROVED" 보낼 수 있음)
        boolean isReject = "반려".equals(decision) || "REJECTED".equalsIgnoreCase(decision);
        boolean isApprove = "승인".equals(decision) || "APPROVED".equalsIgnoreCase(decision);

        // 반려 처리
        if (isReject) {
            reserve.setApprovalState(ApprovalState.REJECTED);
            return;
        }

        if (!isApprove) {
            throw new CustomRestfullException("decision 값이 올바르지 않습니다.", HttpStatus.BAD_REQUEST);
        }

        // 승인 처리
        reserve.setApprovalState(ApprovalState.APPROVED);
        String roomCode = generateRoomCode();
        reserve.setRoomCode(roomCode);

        // 같은 상담 일정의 다른 신청 전부 반려
        rejectOtherReserves(reserve);

        // 상담 일정 예약 완료 처리
        CounselingSchedule schedule = reserve.getCounselingSchedule();
        schedule.setReserved(true);

        // 위험 학생이면 상담 진행 상태로 변경
        if (reserve.getDropoutRisk() != null) {
            DropoutRisk risk = reserve.getDropoutRisk();
            risk.setStatus(RiskStatus.CONSULT_REQ);
            dropoutRiskRepository.save(risk);
        }
    }

    // 같은 상담 일정의 다른 신청 전부 반려
    private void rejectOtherReserves(CounselingReserve approved) {

        List<CounselingReserve> others =
                counselingReserveRepository
                        .findByCounselingSchedule_Id(
                                approved.getCounselingSchedule().getId()
                        );

        for (CounselingReserve r : others) {
            if (!r.getId().equals(approved.getId())
                    && r.getApprovalState() == ApprovalState.REQUESTED) {
                r.setApprovalState(ApprovalState.REJECTED);
            }
        }
    }

    // 학생 기준 상담 예약 목록 조회
    @Transactional(readOnly = true)
    public List<CounselingReserveDto> getStudentReservationList(Long studentId) {

        return counselingReserveRepository.findByStudentId(studentId)
                .stream()
                .map(CounselingReserveDto::new)
                .toList();
    }

    // 교수 기준 상담 예약 목록 조회
    @Transactional(readOnly = true)
    public List<CounselingReserveDto> getProfessorReservationList(Long professorId) {

        return counselingReserveRepository
                .findByCounselingSchedule_Professor_Id(professorId)
                .stream()
                .map(CounselingReserveDto::new)
                .toList();
    }

    // 화상 상담 방 코드 생성
    private String generateRoomCode() {
        long now = System.currentTimeMillis();
        int random = new SecureRandom().nextInt(100);
        return String.format("%03d%02d", now % 1000, random);
    }

    public int getNotApproved(Long professorId) {

        // 1. 교수의 강의 과목 ID 목록 조회
        List<Long> subjectIds = subjectRepository
                .findByProfessor_Id(professorId)
                .stream()
                .map(Subject::getId)
                .toList();

        // 담당 과목이 없으면 빈 리스트
        if (subjectIds.isEmpty()) {
            return 0;
        }

        // 2. 해당 과목들의 미처리 상담 신청 조회
        return counselingReserveRepository
                .findBySubject_IdInAndApprovalState(
                        subjectIds,
                        ApprovalState.REQUESTED
                ).size();
    }

    // 학생 알림용
    @Transactional(readOnly = true)
    public java.util.Map<String, Integer> getMyCounts(Long studentId) {
        int requested = counselingReserveRepository.countByStudent_IdAndApprovalState(studentId, ApprovalState.REQUESTED);
        int approved = counselingReserveRepository.countByStudent_IdAndApprovalState(studentId, ApprovalState.APPROVED);
        return java.util.Map.of("requested", requested, "approved", approved);
    }

    // 교수 -> 학생 상담요청
    @Transactional
    public void professorRequest(CounselingProfessorRequestDto dto, Long professorId) {

        // schedule 검증 (교수 본인 슬롯인지 + 예약 가능인지)
        CounselingSchedule schedule = counselingScheduleRepository.findById(dto.getCounselingScheduleId())
                .orElseThrow(() -> new CustomRestfullException("상담 일정이 존재하지 않습니다.", HttpStatus.BAD_REQUEST));

        if (schedule.getProfessor() == null || schedule.getProfessor().getId() == null
                || !schedule.getProfessor().getId().equals(professorId)) {
            throw new CustomRestfullException("본인의 상담 일정만 요청할 수 있습니다.", HttpStatus.FORBIDDEN);
        }

        if (schedule.isReserved()) {
            throw new CustomRestfullException("이미 예약된 상담 시간입니다.", HttpStatus.BAD_REQUEST);
        }

        // 학생/과목 검증
        Student student = studentRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new CustomRestfullException("대상 학생이 존재하지 않습니다.", HttpStatus.BAD_REQUEST));

        Subject subject = subjectRepository.findById(dto.getSubjectId())
                .orElseThrow(() -> new CustomRestfullException("과목이 존재하지 않습니다.", HttpStatus.BAD_REQUEST));

        // 과목 기준으로도 중복 요청 방지: 학생이 이미 요청했거나, 교수요청이 이미 있으면 막기
        boolean studentRequested =
                counselingReserveRepository.existsByStudent_IdAndSubject_IdAndApprovalStateAndRequester(
                        student.getId(),
                        subject.getId(),
                        ApprovalState.REQUESTED,
                        ReserveRequester.STUDENT
                );
        if (studentRequested) {
            throw new CustomRestfullException("학생이 이미 상담을 신청한 내역이 있습니다.", HttpStatus.CONFLICT);
        }

        boolean professorRequestedSameSubject =
                counselingReserveRepository.existsByStudent_IdAndSubject_IdAndApprovalStateAndRequester(
                        student.getId(),
                        subject.getId(),
                        ApprovalState.REQUESTED,
                        ReserveRequester.PROFESSOR
                );
        if (professorRequestedSameSubject) {
            throw new CustomRestfullException("이미 동일 과목 상담 요청이 존재합니다.", HttpStatus.CONFLICT);
        }

        // 중복 요청 방지: "REQUESTED"만 막고, "REJECTED"는 재요청 가능
        boolean exists = counselingReserveRepository
                .existsByStudent_IdAndCounselingSchedule_IdAndRequesterAndApprovalState(
                        student.getId(),
                        schedule.getId(),
                        ReserveRequester.PROFESSOR,
                        ApprovalState.REQUESTED
                );

        if (exists) {
            throw new CustomRestfullException("이미 동일한 상담 요청이 존재합니다.", HttpStatus.CONFLICT);
        }

        // PreReserve 저장 (CounselingReserve를 그대로 사용)
        CounselingReserve pre = new CounselingReserve();
        pre.setStudent(student);
        pre.setCounselingSchedule(schedule);
        pre.setSubject(subject);
        pre.setReason(dto.getReason());

        // 교수요청 표시/조회/consultState 계산을 위해 반드시 세팅
        pre.setRequester(ReserveRequester.PROFESSOR);

        // 승인 여부
        pre.setApprovalState(ApprovalState.REQUESTED);

        // 위험학생이면 dropoutRisk 연결 (학생+과목의 StuSub 기반)
        attachDropoutRiskIfExists(pre, dto.getStudentId(), dto.getSubjectId());

        counselingReserveRepository.save(pre);
    }

    // 학생: 내가 받은 교수 상담요청 목록
    @Transactional(readOnly = true)
    public Object getMyPreReserveList(Long studentId) {
        List<CounselingReserve> list =
                counselingReserveRepository
                        .findByStudent_IdAndRequesterAndApprovalState(
                                studentId,
                                ReserveRequester.PROFESSOR,
                                ApprovalState.REQUESTED
                        );

        List<CounselPreReserveDto> dtoList = list.stream()
                .map(CounselPreReserveDto::new)
                .collect(Collectors.toList());

        return Map.of("list", dtoList);
    }

    // 학생: 수락 -> reserve 생성
    @Transactional
    public Long acceptPreReserve(Long studentId, Long preReserveId) {
        CounselingReserve pre = getRequestedPreOrThrow(studentId, preReserveId);

        CounselingSchedule schedule = pre.getCounselingSchedule();
        if (schedule == null) {
            throw new CustomRestfullException("상담 일정 정보가 없습니다.", HttpStatus.BAD_REQUEST);
        }

        if (schedule.isReserved()) {
            throw new CustomRestfullException("이미 예약된 상담 시간입니다.", HttpStatus.BAD_REQUEST);
        }

        // pre 자체를 확정 처리 (새 엔티티 만들지 않음)
        pre.setApprovalState(ApprovalState.APPROVED);
        pre.setRoomCode(generateRoomCode());

        // 상담 일정 예약 완료 처리
        schedule.setReserved(true);
        counselingScheduleRepository.save(schedule);

        // 같은 슬롯에 걸린 다른 REQUESTED 신청이 있으면 전부 반려
        rejectOtherReserves(pre);

        // 교수요청 수락으로 확정된 경우도 위험학생 상태 CONSULT_REQ로 동기화
        if (pre.getDropoutRisk() != null) {
            DropoutRisk risk = pre.getDropoutRisk();
            risk.setStatus(RiskStatus.CONSULT_REQ);
            dropoutRiskRepository.save(risk);
        }

        // 저장
        CounselingReserve saved = counselingReserveRepository.save(pre);

        return saved.getId();
    }

    // 학생: 거절
    @Transactional
    public void rejectPreReserve(Long studentId, Long preReserveId) {
        CounselingReserve pre = getRequestedPreOrThrow(studentId, preReserveId);

        pre.setApprovalState(ApprovalState.REJECTED);
        counselingReserveRepository.save(pre);
    }

    // ===================== 중복 제거용 private helper =====================

    // 위험학생이면 dropoutRisk 연결 (학생+과목의 StuSub 기반)
    private void attachDropoutRiskIfExists(CounselingReserve reserve, Long studentId, Long subjectId) {
        StuSub stuSub = stuSubRepository
                .findByStudent_IdAndSubject_Id(studentId, subjectId)
                .orElse(null);

        if (stuSub != null) {
            dropoutRiskRepository.findByStuSub_Id(stuSub.getId()).ifPresent(reserve::setDropoutRisk);
        }
    }

    // 학생 본인 요청 + REQUESTED 상태인 교수요청만 가져오기
    private CounselingReserve getRequestedPreOrThrow(Long studentId, Long preReserveId) {
        CounselingReserve pre = counselingReserveRepository.findById(preReserveId)
                .orElseThrow(() -> new CustomRestfullException("상담 요청이 존재하지 않습니다.", HttpStatus.BAD_REQUEST));

        if (pre.getStudent() == null || pre.getStudent().getId() == null || !pre.getStudent().getId().equals(studentId)) {
            throw new CustomRestfullException("본인에게 온 요청만 처리할 수 있습니다.", HttpStatus.FORBIDDEN);
        }

        if (pre.getApprovalState() != ApprovalState.REQUESTED) {
            throw new CustomRestfullException("이미 처리된 요청입니다.", HttpStatus.BAD_REQUEST);
        }

        return pre;
    }

    // 학생이 확정 상담 취소
    @Transactional
    public void cancelApprovedByStudent(Long studentId, Long reserveId) {
        CounselingReserve reserve = getApprovedReserveOrThrow(reserveId);

        if (reserve.getStudent() == null || reserve.getStudent().getId() == null
                || !reserve.getStudent().getId().equals(studentId)) {
            throw new CustomRestfullException("본인 예약만 취소할 수 있습니다.", HttpStatus.FORBIDDEN);
        }

        cancelApprovedInternal(reserve);
    }

    // 교수가 확정 상담 취소
    @Transactional
    public void cancelApprovedByProfessor(Long professorId, Long reserveId) {
        CounselingReserve reserve = getApprovedReserveOrThrow(reserveId);

        CounselingSchedule schedule = reserve.getCounselingSchedule();
        if (schedule == null || schedule.getProfessor() == null || schedule.getProfessor().getId() == null) {
            throw new CustomRestfullException("상담 일정 정보가 없습니다.", HttpStatus.BAD_REQUEST);
        }

        if (!schedule.getProfessor().getId().equals(professorId)) {
            throw new CustomRestfullException("본인 상담 예약만 취소할 수 있습니다.", HttpStatus.FORBIDDEN);
        }

        cancelApprovedInternal(reserve);
    }

    private void cancelApprovedInternal(CounselingReserve reserve) {
        CounselingSchedule schedule = reserve.getCounselingSchedule();
        if (schedule == null) {
            throw new CustomRestfullException("상담 일정 정보가 없습니다.", HttpStatus.BAD_REQUEST);
        }

        // 슬롯 다시 열기
        schedule.setReserved(false);
        counselingScheduleRepository.save(schedule);

        // 취소 상태로 변경 (이력 남김)
        reserve.setApprovalState(ApprovalState.CANCELED);
        reserve.setRoomCode(null); // 선택 (보안/혼선 방지)

        // DropoutRisk / RiskStatus는 유지 (위험학생 유지 목적)
        counselingReserveRepository.save(reserve);
    }

    private CounselingReserve getApprovedReserveOrThrow(Long reserveId) {
        CounselingReserve reserve = counselingReserveRepository.findById(reserveId)
                .orElseThrow(() -> new CustomRestfullException("상담 예약을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        if (reserve.getApprovalState() != ApprovalState.APPROVED) {
            throw new CustomRestfullException("확정된 상담만 취소할 수 있습니다.", HttpStatus.BAD_REQUEST);
        }

        return reserve;
    }

    // 할당된 방 검증 - 학생
    public boolean isValidRoomStu(Long studentId, String roomCode) {
        long startTime = LocalTime.now().getHour();
        return counselingReserveRepository.existsByStudent_IdAndRoomCodeAndApprovalStateAndCounselingSchedule_StartTimeAndCounselingSchedule_EndTimeAndCounselingSchedule_CounselingDate(studentId, roomCode, ApprovalState.APPROVED, startTime, startTime+1, LocalDate.now());
    }

    // 할당된 방 검증 - 교수
    public boolean isValidRoomPro(Long professorId, String roomCode) {
        List<Subject> subjects = subjectRepository.findByProfessor_Id(professorId);
        List<Long> subjectIds = subjects.stream()
                .map(Subject::getId)
                .toList();
        long startTime = LocalTime.now().getHour();
        return counselingReserveRepository.existsBySubject_IdInAndRoomCodeAndApprovalStateAndCounselingSchedule_StartTimeAndCounselingSchedule_EndTimeAndCounselingSchedule_CounselingDate(subjectIds, roomCode, ApprovalState.APPROVED, startTime, startTime+1, LocalDate.now());
    }

    // 시간 검증( 남은 시간 체크 )
    @Transactional(readOnly = true)
    public Long getEndTimeMinus10(String roomCode) {

        CounselingReserve reserve = counselingReserveRepository
                .findByRoomCodeAndApprovalState(roomCode, ApprovalState.APPROVED);

        if (reserve == null) {
            throw new CustomRestfullException("상담 일정이 없습니다.", HttpStatus.NOT_FOUND);
        }

        return  reserve.getCounselingSchedule().getEndTime();

    }


}