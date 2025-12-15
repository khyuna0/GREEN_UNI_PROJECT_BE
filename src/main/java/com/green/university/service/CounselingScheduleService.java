package com.green.university.service;

import com.green.university.dto.WeeklyCounselingScheduleRequest;
import com.green.university.entity.CounselingSchedule;
import com.green.university.entity.Professor;
import com.green.university.repository.CounselingScheduleRepository;
import com.green.university.repository.ProfessorRepository;
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

    public List<CounselingSchedule> getSchedules (Long professorId) { // 내 상담 목록 불러오기
        Professor professor = professorRepository.findById(professorId).orElseThrow();
        List<CounselingSchedule> lists = scheduleRepository.findByProfessor(professor);
        return lists;
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

}
