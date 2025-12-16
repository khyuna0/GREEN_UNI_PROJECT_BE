package com.green.university.service;

import com.green.university.dto.CounselingInfoDto;
import com.green.university.dto.CounselingPreReserveDto;
import com.green.university.dto.response.CounselingReserveDto;
import com.green.university.entity.*;
import com.green.university.exception.CustomRestfullException;
import com.green.university.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;

@Service
public class CounselingReserveService {

    @Autowired
    private CounselingReserveRepository counselingReserveRepository;
    @Autowired
    private CounselingPreReserveRepository counselingPreReserveRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private SubjectRepository subjectRepository;
    @Autowired
    private StuSubRepository stuSubRepository;
    @Autowired
    private DropoutRiskRepository dropoutRiskRepository;
    @Autowired
    private CounselingScheduleRepository counselingScheduleRepository;

    // 예약 반려 처리 - 가예약의 status 만 바꿔서 업데이트
    public void reject (CounselingPreReserveDto dto) {
        CounselingPreReserve preReserve = counselingPreReserveRepository.findById(dto.getPreReserveId()).orElseThrow(
                () -> new CustomRestfullException("해당 예비 예약을 조회할 수 없습니다.", HttpStatus.NOT_FOUND));
        preReserve.setApprovalState(ReserveStatus.REJECTED);
        counselingPreReserveRepository.save(preReserve);
    }

    // 가예약 확정 처리
    // 1. 가예약 승인
    // 2. 본 예약 생성 + 방 코드 발급
    // 3. 위험 학생이면 상담 진행 상태로 변경
    @Transactional
    public void confirmReservation(CounselingPreReserveDto dto) {

        // ===== 1. 기본 엔티티 조회 =====
        CounselingPreReserve preReserve = getPreReserve(dto.getPreReserveId());
        Student student = getStudent(dto.getStudentId());
        Subject subject = getSubject(dto.getSubjectId());

        // 가예약 승인 처리
        preReserve.setApprovalState(ReserveStatus.APPROVED);
        counselingPreReserveRepository.save(preReserve);

        // ===== 2. 본 예약 생성 =====
        CounselingReserve counselingReserve = new CounselingReserve();
        counselingReserve.setStudent(student);
        counselingReserve.setSubject(subject);
        counselingReserve.setCounselingSchedule(preReserve.getCounselingSchedule());
        counselingReserve.setReason(preReserve.getReason());

        String roomCode = generateRoomCode();
        counselingReserve.setRoomCode(roomCode);

        // ===== 3. 위험 학생 처리 =====
        if(preReserve.getDropoutRisk() != null) {
            DropoutRisk dropoutRisk = preReserve.getDropoutRisk();
            counselingReserve.setDropoutRisk(dropoutRisk);
            dropoutRisk.setStatus(RiskStatus.CONSULT_REQ);
            dropoutRiskRepository.save(dropoutRisk);
        }

        // 같은 시간대 다른 가예약 반려
        rejectOtherPreReserves(preReserve);

        // 상담 스케줄 예약 처리
        markScheduleReserved(preReserve.getCounselingSchedule());

        // ===== 4. 저장 =====
        counselingReserveRepository.save(counselingReserve);
    }

    // 승인된 해당 예약 일정 제외 전부 반려 처리
    private void rejectOtherPreReserves(CounselingPreReserve approved) {

        List<CounselingPreReserve> others =
                counselingPreReserveRepository
                        .findByCounselingSchedule_Id(
                                approved.getCounselingSchedule().getId()
                        );

        for (CounselingPreReserve pre : others) {
            if (!pre.getId().equals(approved.getId())) {
                pre.setApprovalState(ReserveStatus.REJECTED);
            }
        }
    }

    // 교수 상담 스케쥴 상태 변경
    private void markScheduleReserved(CounselingSchedule schedule) {
        schedule.setReserved(true);
    }

    private String generateRoomCode() { // 방 키 생성 메서드
        long now = System.currentTimeMillis();
        int random = new SecureRandom().nextInt(100);
        return String.format("%03d%02d", now % 1000, random);
    }


    private CounselingPreReserve getPreReserve(Long preReserveId) {
        return counselingPreReserveRepository.findById(preReserveId)
                .orElseThrow(() ->
                        new CustomRestfullException(
                                "해당 예비 예약을 조회할 수 없습니다.",
                                HttpStatus.NOT_FOUND
                        )
                );
    }

    private Student getStudent(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() ->
                        new CustomRestfullException(
                                "학생을 찾을 수 없습니다.",
                                HttpStatus.NOT_FOUND
                        )
                );
    }

    private Subject getSubject(Long subjectId) {
        return subjectRepository.findById(subjectId)
                .orElseThrow(() ->
                        new CustomRestfullException(
                                "과목을 찾을 수 없습니다.",
                                HttpStatus.NOT_FOUND
                        )
                );
    }

    // 학생 - 내 본 예약 목록 불러오기
    public List<CounselingReserveDto> getReservationList (Long id) {

        List<CounselingReserve> entity = counselingReserveRepository.findByStudentId(id);

        return entity.stream()
                .map(CounselingReserveDto::new)
                .toList();
    }

    // 교수 - 내 예약 불러오기
    public List<CounselingReserveDto> getReservationListProfessor (Long id) {

        List<CounselingSchedule> approvedSchedules = counselingScheduleRepository.findByProfessor_IdAndReserved(id, true);

        List<Long> scheduleIds = approvedSchedules.stream()
                .map(CounselingSchedule::getId)
                .toList();

        List<CounselingReserve> reserves =
                counselingReserveRepository.findByCounselingSchedule_IdIn(scheduleIds);

        return reserves.stream()
                .map(CounselingReserveDto::new)
                .toList();
    }
}
