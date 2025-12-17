//package com.green.university.infra.ai.deprecated;
//
//import com.green.university.domain.dropoutrisk.respository.DropoutRiskRepository;
//import com.green.university.domain.student.entity.Student;
//import com.green.university.domain.subject.entity.Subject;
//import com.green.university.domain.dropoutrisk.entity.DropoutRisk;
//import com.green.university.domain.dropoutrisk.entity.RiskLevel;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.context.ApplicationEventPublisher;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//// (upsert + 이벤트 발행)
//public class RiskEvaluatorService {
//
//    private final DropoutRiskRepository dropoutRiskRepository;
//    private final ApplicationEventPublisher publisher;
//
//    @Transactional
//    public void evaluateAttendance(Student student, Subject subject, long totalAbsent) {
//        RiskLevel level = (totalAbsent >= 3) ? RiskLevel.DANGER
//                : (totalAbsent == 2) ? RiskLevel.WARNING
//                : null;
//        if (level == null) return;
//
//        String aiInput = "ATTENDANCE totalAbsent=" + totalAbsent + ", level=" + level;
//
//    }
//
//    @Transactional
//    public void evaluateSubjectGrade(Student student, Subject subject, String gradeLetter) {
//        RiskLevel level = gradeToLevel(gradeLetter);
//        if (level == null) return;
//
//        String aiInput = "SUBJECT_GRADE grade=" + gradeLetter + ", level=" + level;
//
//    }
//
//    @Transactional
//    public void evaluateSemesterGpa(Student student, float semesterGpa, Long year, Long semester) {
//        RiskLevel level = (semesterGpa < 3.0f) ? RiskLevel.DANGER
//                : (semesterGpa < 3.3f) ? RiskLevel.WARNING
//                : null;
//        if (level == null) return;
//
//        String aiInput = "SEMESTER_GPA y=" + year + ", s=" + semester + ", gpa=" + semesterGpa + ", level=" + level;
//    }
//
//    private boolean changed(DropoutRisk risk, RiskLevel level, String aiInput) {
//        if (risk.getRiskLevel() != level) return true;
//        if (risk.getLastAiInput() == null) return true;
//        return !risk.getLastAiInput().equals(aiInput);
//    }
//
//    // C는 WARNING, D/F는 DANGER
//    private RiskLevel gradeToLevel(String g) {
//        if (g == null) return null;
//
//        if (g.startsWith("C")) return RiskLevel.WARNING;
//
//        if (g.startsWith("D") || g.equals("F")) return RiskLevel.DANGER;
//
//        return null; // A/B는 정상
//    }
//}
//
