package com.green.university.domain.counseling.service;

import com.green.university.domain.counseling.dto.CounselingReserveDto;
import com.green.university.domain.counseling.dto.CounselingReserveRequestDto;
import com.green.university.domain.counseling.entity.ApprovalState;
import com.green.university.domain.counseling.entity.CounselingReserve;
import com.green.university.domain.counseling.entity.CounselingSchedule;
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
import com.green.university.infra.ai.DropoutRiskRepository;
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
    public void requestReserve(CounselingReserveRequestDto dto, Long studentId) {

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
        reserve.setRoomCode(generateRoomCode());

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
}
