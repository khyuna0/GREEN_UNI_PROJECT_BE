package com.green.university.domain.counseling.service;

import com.green.university.domain.counseling.dto.CounselPreReserveDto;
import com.green.university.domain.counseling.dto.CounselingProfessorRequestDto;
import com.green.university.domain.counseling.dto.CounselingReserveDto;
import com.green.university.domain.counseling.dto.CounselingStudentRequestDto;
import com.green.university.domain.counseling.entity.ApprovalState;
import com.green.university.domain.counseling.entity.CounselingPreReserve;
import com.green.university.domain.counseling.entity.CounselingReserve;
import com.green.university.domain.counseling.entity.CounselingSchedule;
import com.green.university.domain.counseling.repository.CounselingPreReserveRepository;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class CounselingReserveService {

    @Autowired
    private CounselingReserveRepository counselingReserveRepository;
    @Autowired
    private CounselingPreReserveRepository counselingPreReserveRepository;
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

        // 이미 신청한 상담인지 체크
        boolean alreadyReserved =
                counselingReserveRepository.existsByStudent_IdAndCounselingSchedule_Id(
                        studentId,
                        dto.getCounselingScheduleId()
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

        // 학생 + 과목 기준으로 StuSub 조회
        StuSub stuSub = stuSubRepository
                .findByStudent_IdAndSubject_Id(studentId, dto.getSubjectId())
                .orElse(null);

        // 해당 과목의 위험 학생이면 DropoutRisk 연결
        if (stuSub != null) {
            dropoutRiskRepository
                    .findByStuSub_Id(stuSub.getId())
                    .ifPresent(reserve::setDropoutRisk);
        }

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

        // 반려 처리
        if ("반려".equals(decision)) {
            reserve.setApprovalState(ApprovalState.REJECTED);
            return;
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
        int approved  = counselingReserveRepository.countByStudent_IdAndApprovalState(studentId, ApprovalState.APPROVED);
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

        // 중복 요청 방지 (REQUESTED 상태로 동일 슬롯 요청이 이미 있으면 막기)
        boolean exists = counselingPreReserveRepository
                .existsByStudent_IdAndCounselingSchedule_IdAndApprovalState(
                        student.getId(),
                        schedule.getId(),
                        ApprovalState.REQUESTED
                );

        if (exists) {
            throw new CustomRestfullException("이미 동일한 상담 요청이 존재합니다.", HttpStatus.CONFLICT);
        }

        // PreReserve 저장
        CounselingPreReserve pre = new CounselingPreReserve();
        pre.setStudent(student);
        pre.setCounselingSchedule(schedule);
        pre.setSubject(subject);
        pre.setReason(dto.getReason());
        // 학생 위험 상태 조인/위험학생 아니면 null
        // pre.setDropoutRisk(...);

        // 위험학생이면 dropoutRisk 연결 (학생+과목의 StuSub 기반)
        StuSub stuSub = stuSubRepository
                .findByStudent_IdAndSubject_Id(dto.getStudentId(), dto.getSubjectId())
                .orElse(null);

        if (stuSub != null) {
            dropoutRiskRepository.findByStuSub_Id(stuSub.getId()).ifPresent(pre::setDropoutRisk);
        }

        // 승인 여부
        pre.setApprovalState(ApprovalState.REQUESTED);
        // 학생 신청, 교수 승인(승인 시 예약 생성), 반려

        counselingPreReserveRepository.save(pre);

        // 교수 요청을 보낸 순간부터 교수 화면에서 버튼이 사라지게(상태 DETECTED -> CONSULT_REQ)
        if (pre.getDropoutRisk() != null) {
            DropoutRisk risk = pre.getDropoutRisk();
            risk.setStatus(RiskStatus.CONSULT_REQ);
            dropoutRiskRepository.save(risk);
        }
    }

    // 학생: 내가 받은 교수 상담요청 목록
    @Transactional(readOnly = true)
    public Object getMyPreReserveList(Long studentId) {
        List<CounselingPreReserve> list =
                counselingPreReserveRepository
                        .findByStudent_IdAndApprovalStateOrderByCounselingSchedule_CounselingDateAscCounselingSchedule_StartTimeAsc(
                                studentId,
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
        CounselingPreReserve pre = counselingPreReserveRepository.findById(preReserveId)
                .orElseThrow(() -> new CustomRestfullException("상담 요청이 존재하지 않습니다.", HttpStatus.BAD_REQUEST));

        if (pre.getStudent() == null || pre.getStudent().getId() == null || !pre.getStudent().getId().equals(studentId)) {
            throw new CustomRestfullException("본인에게 온 요청만 처리할 수 있습니다.", HttpStatus.FORBIDDEN);
        }

        if (pre.getApprovalState() != ApprovalState.REQUESTED) {
            throw new CustomRestfullException("이미 처리된 요청입니다.", HttpStatus.BAD_REQUEST);
        }

        CounselingSchedule schedule = pre.getCounselingSchedule();
        if (schedule == null) {
            throw new CustomRestfullException("상담 일정 정보가 없습니다.", HttpStatus.BAD_REQUEST);
        }

        if (schedule.isReserved()) {
            throw new CustomRestfullException("이미 예약된 상담 시간입니다.", HttpStatus.BAD_REQUEST);
        }

        // pre 상태 변경
        pre.setApprovalState(ApprovalState.APPROVED);

        // reserve 생성 (교수가 요청한 건 학생 수락 시점에 확정처리)
        CounselingReserve reserve = CounselingReserve.builder()
                .student(pre.getStudent())
                .subject(pre.getSubject())
                .counselingSchedule(schedule)
                .reason(pre.getReason())
                .approvalState(ApprovalState.APPROVED)
                .roomCode(generateRoomCode())
                .dropoutRisk(pre.getDropoutRisk())
                .build();

        // 3) schedule reserved 처리
        schedule.setReserved(true);

        CounselingReserve saved = counselingReserveRepository.save(reserve);

        // 같은 슬롯에 걸린 다른 REQUESTED 신청이 있으면 전부 반려
        rejectOtherReserves(saved);

        counselingPreReserveRepository.save(pre);
        counselingScheduleRepository.save(schedule);

        // 위험 학생이면 상담 진행 상태로 변경
        if (saved.getDropoutRisk() != null) {
            DropoutRisk risk = saved.getDropoutRisk();
            risk.setStatus(RiskStatus.CONSULT_REQ);
            dropoutRiskRepository.save(risk);
        }

        return saved.getId();
    }

    // 학생: 거절
    @Transactional
    public void rejectPreReserve(Long studentId, Long preReserveId) {
        CounselingPreReserve pre = counselingPreReserveRepository.findById(preReserveId)
                .orElseThrow(() -> new CustomRestfullException("상담 요청이 존재하지 않습니다.", HttpStatus.BAD_REQUEST));

        if (pre.getStudent() == null || pre.getStudent().getId() == null || !pre.getStudent().getId().equals(studentId)) {
            throw new CustomRestfullException("본인에게 온 요청만 처리할 수 있습니다.", HttpStatus.FORBIDDEN);
        }

        if (pre.getApprovalState() != ApprovalState.REQUESTED) {
            throw new CustomRestfullException("이미 처리된 요청입니다.", HttpStatus.BAD_REQUEST);
        }

        pre.setApprovalState(ApprovalState.REJECTED);
        counselingPreReserveRepository.save(pre);
    }
}
