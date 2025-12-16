package com.green.university.service;

import com.green.university.entity.DropoutRisk;
import com.green.university.entity.Subject;
import com.green.university.repository.DropoutRiskRepository;
import com.green.university.repository.SubjectRepository;
import com.green.university.utils.Define;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class RiskStudentService {

    @Autowired
    private DropoutRiskRepository dropoutRiskRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    public List<DropoutRisk> getRiskStudents(Long professorId) {

        List<Subject> subjectList =
                subjectRepository.findByProfessor_IdAndSubYearAndSemester(
                        professorId, Define.CURRENT_YEAR, Define.CURRENT_SEMESTER
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