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
    public void professorRequest(CounselingProfessorRequestDto dto, Long professorId) {

        CounselingSchedule schedule = counselingScheduleRepository.findById(dto.getCounselingScheduleId())
                .orElseThrow(() -> new CustomRestfullException("상담 일정을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        if (schedule.getProfessor() == null || !schedule.getProfessor().getId().equals(professorId)) {
            throw new CustomRestfullException("본인 상담 일정만 요청할 수 있습니다.", HttpStatus.FORBIDDEN);
        }

        if (schedule.isReserved()) {
            throw new CustomRestfullException("이미 예약 완료된 일정입니다.", HttpStatus.BAD_REQUEST);
        }

        Student student = studentRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new CustomRestfullException("학생을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        Subject subject = subjectRepository.findById(dto.getSubjectId())
                .orElseThrow(() -> new CustomRestfullException("과목을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        // PreReserve 기준으로 중복 체크
        boolean exists = counselingPreReserveRepository
                .existsByStudent_IdAndCounselingSchedule_IdAndApprovalState(
                        student.getId(),
                        schedule.getId(),
                        ApprovalState.REQUESTED
                );

        if (exists) {
            throw new CustomRestfullException("이미 해당 학생에게 같은 상담 요청이 존재합니다.", HttpStatus.BAD_REQUEST);
        }

        CounselingPreReserve pre = new CounselingPreReserve();
        pre.setStudent(student);
        pre.setSubject(subject);
        pre.setCounselingSchedule(schedule);
        pre.setReason(dto.getReason());
        pre.setApprovalState(ApprovalState.REQUESTED);

        StuSub stuSub = stuSubRepository
                .findByStudent_IdAndSubject_Id(student.getId(), subject.getId())
                .orElse(null);

        if (stuSub != null) {
            dropoutRiskRepository.findByStuSub_Id(stuSub.getId()).ifPresent(pre::setDropoutRisk);
        }

        // PreReserve 저장
        counselingPreReserveRepository.save(pre);
    }


    // 학생 : 내가 받은 교수의 상담 요청
    @Transactional(readOnly = true)
    public List<CounselPreReserveDto> getMyPreReserveList(Long studentId) {

        return counselingPreReserveRepository
                .findByStudent_IdAndApprovalState(studentId, ApprovalState.REQUESTED)
                .stream()
                .map(CounselPreReserveDto::new)
                .toList();
    }

    //학생이 신청 수락시 상담 예약 확정
    public Long acceptPreReserve(Long studentId, Long preReserveId) {

        CounselingPreReserve pre = counselingPreReserveRepository.findById(preReserveId)
                .orElseThrow(() ->
                        new CustomRestfullException("상담 요청을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
                );

        // 본인 요청인지 체크
        if (pre.getStudent() == null || !pre.getStudent().getId().equals(studentId)) {
            throw new CustomRestfullException("본인에게 온 요청만 수락할 수 있습니다.", HttpStatus.FORBIDDEN);
        }

        // 이미 처리된 요청이면 막기
        if (pre.getApprovalState() != ApprovalState.REQUESTED) {
            throw new CustomRestfullException("이미 처리된 상담 요청입니다.", HttpStatus.BAD_REQUEST);
        }

        // 이미 같은 슬롯으로 reserve가 존재하면 막기
        boolean alreadyReserved =
                counselingReserveRepository.existsByStudent_IdAndCounselingSchedule_Id(
                        studentId,
                        pre.getCounselingSchedule().getId()
                );

        if (alreadyReserved) {
            throw new CustomRestfullException("이미 해당 상담 일정에 신청한 내역이 있습니다.", HttpStatus.BAD_REQUEST);
        }

        // reserve 생성 (교수 승인 대기)
        CounselingReserve reserve = new CounselingReserve();
        reserve.setStudent(pre.getStudent());
        reserve.setSubject(pre.getSubject());
        reserve.setCounselingSchedule(pre.getCounselingSchedule());
        reserve.setReason(pre.getReason());
        reserve.setApprovalState(ApprovalState.REQUESTED);
        reserve.setDropoutRisk(pre.getDropoutRisk());

        counselingReserveRepository.save(reserve);

        // preReserve는 학생 수락 완료로 표시 (같은 enum 재사용)
        pre.setApprovalState(ApprovalState.APPROVED);

        return reserve.getId();
    }

    // 학생 : 교수의 상담요청 거절 -> Reject
    public void rejectPreReserve(Long studentId, Long preReserveId) {

        CounselingPreReserve pre = counselingPreReserveRepository.findById(preReserveId)
                .orElseThrow(() ->
                        new CustomRestfullException("상담 요청을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
                );


        if (pre.getApprovalState() != ApprovalState.REQUESTED) {
            throw new CustomRestfullException("이미 처리된 상담 요청입니다.", HttpStatus.BAD_REQUEST);
        }

        pre.setApprovalState(ApprovalState.REJECTED);
    }




}
