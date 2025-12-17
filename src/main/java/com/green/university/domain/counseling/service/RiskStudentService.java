package com.green.university.domain.counseling.service;

import com.green.university.domain.subject.entity.Subject;
import com.green.university.domain.subject.repository.SubjectRepository;
import com.green.university.global.utils.Define;
import com.green.university.domain.dropoutrisk.respository.DropoutRiskRepository;
import com.green.university.domain.dropoutrisk.entity.DropoutRisk;
import com.green.university.global.utils.TermUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RiskStudentService {

    @Autowired
    private DropoutRiskRepository dropoutRiskRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    public List<DropoutRisk> getRiskStudents(Long professorId) {

        List<Subject> subjectList =
                subjectRepository.findByProfessor_IdAndSubYearAndSemester(
                        professorId, TermUtil.currentYear(), TermUtil.currentSemester()
                );

        List<DropoutRisk> dropoutRisks = new ArrayList<>();

        for (Subject subject : subjectList) {
            Long subjectId = subject.getId();

            List<DropoutRisk> risksBySubject =
                    dropoutRiskRepository.findByStuSub_Subject_Id(subjectId);

            dropoutRisks.addAll(risksBySubject);
        }
        return dropoutRisks;
    }

}