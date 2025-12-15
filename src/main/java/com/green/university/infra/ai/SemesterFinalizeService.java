package com.green.university.infra.ai;

import com.green.university.domain.grade.service.GradeService;
import com.green.university.domain.grade.dto.MyGradeDto;
import com.green.university.domain.student.entity.Student;
import com.green.university.domain.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
// 버튼 기능 서비스 (학기 최종 ai 상담을 위한 ..)
public class SemesterFinalizeService {

    private final StudentRepository studentRepository;
    private final GradeService gradeService;
    private final RiskEvaluatorService riskEvaluatorService;

    @Transactional
    public int finalizeSemester(Long year, Long semester, Long studentId) {

        List<Student> targets = (studentId != null)
                ? List.of(studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("학생 없음")))
                : studentRepository.findAll();

        int processed = 0;

        for (Student s : targets) {
            MyGradeDto myGrade = gradeService.readMyGradeByStudentId(s.getId());
            if (myGrade == null) continue;

            // 여기서 위험 판정 + risk upsert + 이벤트 발행까지 됨
            riskEvaluatorService.evaluateSemesterGpa(s, myGrade.getAverage(), year, semester);
            processed++;
        }

        return processed;
    }
}
