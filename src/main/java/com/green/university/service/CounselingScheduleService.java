package com.green.university.service;

import com.green.university.dto.CounselingInfoDto;
import com.green.university.dto.WeeklyCounselingScheduleRequest;
import com.green.university.entity.CounselingSchedule;
import com.green.university.entity.Professor;
import com.green.university.entity.Subject;
import com.green.university.repository.CounselingScheduleRepository;
import com.green.university.repository.ProfessorRepository;
import com.green.university.repository.SubjectRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class CounselingScheduleService {

    @Autowired
    private CounselingScheduleRepository scheduleRepository;

    @Autowired
    private ProfessorRepository professorRepository;

    @Autowired
    private SubjectRepository subjectRepository;
    
    // 교수 id로 상담 목록 불러오기
    public List<CounselingSchedule> getSchedulesByWeek ( Long professorId,
                                                         LocalDate start,
                                                         LocalDate end) { // 내 상담 목록 불러오기
        List<CounselingSchedule> lists = scheduleRepository.findByProfessor_IdAndCounselingDateBetween(professorId, start, end);
        return lists;
    }

    // 과목 아이디로 교수 찾아 상담 목록 불러오기
    public List<CounselingInfoDto> getSchedulesByWeekAndSubId (Long subId,
                                                               LocalDate start,
                                                               LocalDate end) {
        Subject subject = subjectRepository.findById(subId).orElseThrow();
        Long professorId = subject.getProfessor().getId();
        List<CounselingSchedule> schedulList = scheduleRepository.findByProfessor_IdAndCounselingDateBetween(professorId, start, end);
        List<CounselingInfoDto> dtos =
                schedulList.stream()
                        .map(CounselingInfoDto::new)
                        .toList();
        return dtos;
    }

    public void createWeeklySchedule(
            Long professorId,
            WeeklyCounselingScheduleRequest request
    ) {
        Professor professor = professorRepository.findById(professorId)
                .orElseThrow();

        for (Map.Entry<LocalDate, List<Long>> entry : request.getSlots().entrySet()) {

            LocalDate date = entry.getKey();
            String dayOfWeek = date.getDayOfWeek().name();

            for (Long startTime : entry.getValue()) {

                boolean exists =
                        scheduleRepository
                                .existsByProfessorIdAndCounselingDateAndStartTime(
                                        professorId, date, startTime
                                );

                if (exists) {
                    // 이미 열린 슬롯 → 스킵 or 예외
                    continue; // 권장: 중복은 무시
                }

                CounselingSchedule cs = new CounselingSchedule();
                cs.setProfessor(professor);
                cs.setSubYear(request.getSubYear());
                cs.setSemester(request.getSemester());
                cs.setCounselingDate(date);
                cs.setDayOfWeek(dayOfWeek);
                cs.setStartTime(startTime);
                cs.setEndTime(startTime + 1);

                scheduleRepository.save(cs);
            }
        }
    }

    @Transactional
    public void deleteSchedules(Long professorId, LocalDate date, Long startTime) {
        CounselingSchedule schedule =
                scheduleRepository
                        .findByProfessor_IdAndCounselingDateAndStartTime(
                                professorId, date, startTime
                        );

        scheduleRepository.delete(schedule);
    }


}
