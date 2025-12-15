package com.green.university.infra.ai;

import com.green.university.domain.student.entity.Student;
import com.green.university.domain.subject.entity.Subject;
import com.green.university.infra.ai.entity.DropoutRisk;
import com.green.university.infra.ai.entity.RiskLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
// (upsert + 이벤트 발행)
public class RiskEvaluatorService {

    private final DropoutRiskRepository dropoutRiskRepository;
    private final ApplicationEventPublisher publisher;

    @Transactional
    public void evaluateAttendance(Student student, Subject subject, long totalAbsent) {
        RiskLevel level = (totalAbsent >= 3) ? RiskLevel.DANGER
                : (totalAbsent == 2) ? RiskLevel.WARNING
                : null;
        if (level == null) return;

        String aiInput = "ATTENDANCE totalAbsent=" + totalAbsent + ", level=" + level;

//        DropoutRisk risk = dropoutRiskRepository
//                .findByStudent_IdAndSubject_IdAndRiskType(student.getId(), subject.getId(), RiskType.ATTENDANCE)
//                .orElseGet(() -> DropoutRisk.builder()
//                        .student(student)
//                        .subject(subject)
//                        .riskType(RiskType.ATTENDANCE)
//                        .status(RiskStatus.DETECTED)
//                        .build());
//
//        boolean changed = changed(risk, level, aiInput);
//
//        risk.setRiskLevel(level);
//        risk.setLastAiInput(aiInput);
//
//        DropoutRisk saved = dropoutRiskRepository.save(risk);
//
//        // WARNING도 미리 상담 원하면 WARNING도 AI 돌리자
//        if (changed) {
//            publisher.publishEvent(new RiskChangedEvent(saved.getId()));
//        }
    }

    @Transactional
    public void evaluateSubjectGrade(Student student, Subject subject, String gradeLetter) {
        RiskLevel level = gradeToLevel(gradeLetter);
        if (level == null) return;

        String aiInput = "SUBJECT_GRADE grade=" + gradeLetter + ", level=" + level;

//        DropoutRisk risk = dropoutRiskRepository
//                .findByStudent_IdAndSubject_IdAndRiskType(student.getId(), subject.getId(), RiskType.SUBJECT_GRADE)
//                .orElseGet(() -> DropoutRisk.builder()
//                        .student(student)
//                        .subject(subject)
//                        .riskType(RiskType.SUBJECT_GRADE)
//                        .status(RiskStatus.DETECTED)
//                        .build());
//
//        boolean changed = changed(risk, level, aiInput);
//
//        risk.setRiskLevel(level);
//        risk.setLastAiInput(aiInput);
//
//        DropoutRisk saved = dropoutRiskRepository.save(risk);
//
//        if (changed) {
//            publisher.publishEvent(new RiskChangedEvent(saved.getId()));
//        }
    }

    @Transactional
    public void evaluateSemesterGpa(Student student, float semesterGpa, Long year, Long semester) {
        RiskLevel level = (semesterGpa < 3.0f) ? RiskLevel.DANGER
                : (semesterGpa < 3.3f) ? RiskLevel.WARNING
                : null;
        if (level == null) return;

        String aiInput = "SEMESTER_GPA y=" + year + ", s=" + semester + ", gpa=" + semesterGpa + ", level=" + level;

//        DropoutRisk risk = dropoutRiskRepository
//                .findByStudent_IdAndSubjectIsNullAndRiskType(student.getId(), RiskType.SEMESTER_GPA)
//                .orElseGet(() -> DropoutRisk.builder()
//                        .student(student)
//                        .subject(null)
//                        .riskType(RiskType.SEMESTER_GPA)
//                        .status(RiskStatus.DETECTED)
//                        .build());
//
//        boolean changed = changed(risk, level, aiInput);
//
//        risk.setRiskLevel(level);
//        risk.setLastAiInput(aiInput);
//
//        DropoutRisk saved = dropoutRiskRepository.save(risk);
//
//        if (changed) {
//            publisher.publishEvent(new RiskChangedEvent(saved.getId()));
//        }
    }

    private boolean changed(DropoutRisk risk, RiskLevel level, String aiInput) {
        if (risk.getRiskLevel() != level) return true;
        if (risk.getLastAiInput() == null) return true;
        return !risk.getLastAiInput().equals(aiInput);
    }

    // C는 WARNING, D/F는 DANGER
    private RiskLevel gradeToLevel(String g) {
        if (g == null) return null;

        if (g.startsWith("C")) return RiskLevel.WARNING;

        if (g.startsWith("D") || g.equals("F")) return RiskLevel.DANGER;

        return null; // A/B는 정상
    }
}

