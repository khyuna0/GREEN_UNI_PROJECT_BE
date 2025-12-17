package com.green.university.domain.counseling.service;

import com.green.university.domain.counseling.dto.CounselingInfoDto;
import com.green.university.domain.counseling.dto.WeeklyCounselingScheduleRequest;
import com.green.university.domain.counseling.entity.CounselingSchedule;
import com.green.university.domain.counseling.repository.CounselingScheduleRepository;
import com.green.university.domain.professor.entity.Professor;
import com.green.university.domain.professor.repository.ProfessorRepository;
import com.green.university.domain.subject.entity.Subject;
import com.green.university.domain.subject.repository.SubjectRepository;
import com.green.university.global.exception.CustomRestfullException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class CounselingScheduleService {

    @Autowired
    private CounselingScheduleRepository counselingScheduleRepository;
    @Autowired
    private ProfessorRepository professorRepository;
    @Autowired
    private SubjectRepository subjectRepository;

    // 교수 id로 상담 목록 불러오기
    public List<CounselingSchedule> getSchedulesByWeek(Long professorId,
                                                       LocalDate start,
                                                       LocalDate end) { // 내 상담 목록 불러오기
        List<CounselingSchedule> lists = counselingScheduleRepository.findByProfessor_IdAndCounselingDateBetween(professorId, start, end);
        return lists;
    }

    // 과목 아이디로 교수 찾아 상담 목록 불러오기
    public List<CounselingInfoDto> getSchedulesByWeekAndSubId(Long subId,
                                                              LocalDate start,
                                                              LocalDate end) {
        Subject subject = subjectRepository.findById(subId).orElseThrow();
        Long professorId = subject.getProfessor().getId();
        List<CounselingSchedule> schedulList = counselingScheduleRepository.findByProfessor_IdAndCounselingDateBetween(professorId, start, end);
        return schedulList.stream()
                .map(CounselingInfoDto::new)
                .toList();
    }

    @Transactional
    public void createWeeklySchedule(
            Long professorId,
            WeeklyCounselingScheduleRequest request
    ) {
        // 교수 조회
        Professor professor = professorRepository.findById(professorId)
                .orElseThrow(() ->
                        new CustomRestfullException(
                                "교수를 찾을 수 없습니다.",
                                HttpStatus.NOT_FOUND
                        )
                );

        // 프론트에서 넘어온 슬롯 기준
        for (Map.Entry<LocalDate, List<Long>> entry : request.getSlots().entrySet()) {

            LocalDate date = entry.getKey();
            String dayOfWeek = date.getDayOfWeek().name();

            for (Long startTime : entry.getValue()) {

                // 이미 존재하는 슬롯인지 확인
                boolean exists =
                        counselingScheduleRepository
                                .existsByProfessor_IdAndCounselingDateAndStartTime(
                                        professorId,
                                        date,
                                        startTime
                                );

                // 이미 있으면 아무 것도 안 함 (중복 INSERT 방지)
                if (exists) {
                    continue;
                }

                // 새 슬롯만 INSERT
                CounselingSchedule cs = new CounselingSchedule();
                cs.setProfessor(professor);
                cs.setSubYear(request.getSubYear());
                cs.setSemester(request.getSemester());
                cs.setCounselingDate(date);
                cs.setDayOfWeek(dayOfWeek);
                cs.setStartTime(startTime);
                cs.setEndTime(startTime + 1);
                cs.setReserved(false);

                counselingScheduleRepository.save(cs);
            }
        }
    }


    @Transactional
    public void deleteSchedules(Long professorId, LocalDate date, Long startTime) {

        CounselingSchedule schedule =
                counselingScheduleRepository
                        .findByProfessor_IdAndCounselingDateAndStartTime(
                                professorId,
                                date,
                                startTime
                        );

        // 이미 없으면 그냥 종료
        if (schedule == null) {
            return;
        }

        //예약된 일정이면 삭제 막기
        if (schedule.isReserved()) {
            throw new CustomRestfullException("예약된 일정은 삭제할 수 없습니다.", HttpStatus.BAD_REQUEST);
        }

        counselingScheduleRepository.delete(schedule);
    }


    public Map<String, Object> getSchedulesBySubject(Long subjectId) {

        // 1. 과목 조회
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() ->
                        new CustomRestfullException("과목을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
                );

        // 2. 과목 담당 교수 ID
        Long professorId = subject.getProfessor().getId();

        // 3. 교수의 상담 일정 중 예약 안 된 것만 조회
        List<CounselingSchedule> schedules =
                counselingScheduleRepository
                        .findByProfessor_IdAndReservedFalse(professorId);

        return Map.of(
                "subjectName", subject.getName(),
                "scheduleList", schedules
        );
    }

    // 포탈 알림 용 - 교수의 오늘 상담 일정
    public int counselingNumByDate(Long professorId) {
        return counselingScheduleRepository.findByProfessor_IdAndCounselingDate(professorId, LocalDate.now()).size();
    }


}
