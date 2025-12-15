package com.green.university.domain.subject.service;

import com.green.university.domain.grade.entity.Grade;
import com.green.university.domain.grade.repository.GradeRepository;
import com.green.university.domain.subject.dto.PenaltyResultFormDto;
import com.green.university.domain.subject.entity.StuSub;
import com.green.university.domain.subject.entity.StuSubDetail;
import com.green.university.domain.subject.repository.StuSubDetailRepository;
import com.green.university.domain.subject.repository.StuSubRepository;
import com.green.university.domain.subject.repository.SubjectRepository;
import com.green.university.global.exception.CustomRestfullException;
import com.green.university.global.utils.PenaltyCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StuSubDetailService {

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private StuSubDetailRepository stuSubDetailRepository;

    @Autowired
    private StuSubRepository stuSubRepository;

    @Autowired
    private StuSubService stuSubService;

    @Autowired
    private GradeRepository gradeRepository;

    // 상대평가 등급 계산
    // 결석 5번 이상인 학생은 이미 F 처리 되어있음
    public void getRelativeGrade(Long subjectId) {

        // 해당 과목의 전체 학생 수
        int numOfStudent = subjectRepository.findNumOfStudentById(subjectId);

        // 성적 리스트 조회 (환산점수 높은 순 정렬)
        List<StuSubDetail> stuSubList = stuSubDetailRepository
                .findBySubject_IdOrderByConvertedMarkDesc(subjectId);

        // 성적 미기입 학생 체크
        for (StuSubDetail d : stuSubList) {
            if (d.getConvertedMark() == null) {
                throw new CustomRestfullException(
                        "성적이 기입되지 않은 학생이 있어 등급을 산출할 수 없습니다.",
                        HttpStatus.BAD_REQUEST
                );
            }
        }

        // 상대평가 성적 산출, 비율은 조정가능함
        int aCount = (int) Math.round(numOfStudent * 0.20);  // 20%
        int bCount = (int) Math.round(numOfStudent * 0.50);  // 50%
        int cCount = (int) Math.round(numOfStudent * 0.20);  // 20%
        int dCount = numOfStudent - (aCount + bCount + cCount); // 나머지 : D 구간

        int aPlus = (int)Math.round(aCount * 0.4);  // A 중 상위 40%
        int bPlus = (int)Math.round(bCount * 0.4);
        int cPlus = (int)Math.round(cCount * 0.4);
        int dPlus = (int)Math.round(dCount * 0.4);



        for (int i = 0; i< numOfStudent; i++) {
            StuSubDetail d = stuSubList.get(i);
            if (d.getGrade() != null && d.getGrade().equals("F")) continue; // 이미 F면 건너뜀

            String getGrade;
            // 결석 수 계산
            PenaltyResultFormDto penaltyResult = PenaltyCalculator.calculate(d.getAbsent(), d.getLateness());
            long totalAbsent = penaltyResult.getTotalAbsent();

            if (i < aCount) {
                // A 구간
                if (i < aPlus) getGrade = "A+";
                else getGrade = "A0";

            } else if (i < aCount + bCount) {
                // B 구간
                int idxInB = i - aCount;
                if (idxInB < bPlus) getGrade = "B+";
                else getGrade = "B0";

            } else if (i < aCount + bCount + cCount) {
                // C 구간
                int idxInC = i - (aCount + bCount);
                if (idxInC < cPlus) getGrade = "C+";
                else getGrade = "C0";

            } else {
                // D 구간
                int idxInD = i - (aCount + bCount + cCount);
                if (idxInD < dPlus) getGrade = "D+";
                else getGrade = "D0";
            }

            if(totalAbsent > 5 || d.getMildExam() < 40 || d.getFinalExam() < 40 || d.getConvertedMark() < 60) {
                getGrade = "F";
                }

            Grade grade = gradeRepository.findByGrade(getGrade)
                    .orElseThrow(() -> new RuntimeException("존재하지 않는 학점 등급입니다"));
            StuSub stuSub = stuSubRepository
                    .findByStudent_IdAndSubject_Id(d.getStudent().getId(), subjectId)
                    .orElseThrow(() -> new RuntimeException("학생 과목 정보 없음"));
            stuSub.setGrade(grade);
            d.setGrade(grade.getGrade()); // 단순 출력용으로 저장함

            stuSubService.updateCompleteGrade(d.getStudent().getId(), subjectId);

        }

    }
}
