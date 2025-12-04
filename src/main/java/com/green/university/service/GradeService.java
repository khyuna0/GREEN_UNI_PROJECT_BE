package com.green.university.service;

import com.green.university.dto.response.GradeDto;
import com.green.university.dto.response.GradeForScholarshipDto;
import com.green.university.dto.response.MyGradeDto;
import com.green.university.entity.Grade;
import com.green.university.entity.StuSub;
import com.green.university.entity.Subject;
import com.green.university.repository.interfaces.GradeRepository;
import com.green.university.repository.interfaces.StuSubRepository;
import com.green.university.utils.Define;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class GradeService {

    @Autowired
    private StuSubRepository stuSubRepository;

    @Autowired
    private GradeRepository gradeRepository;


    // 학생이 수강신청한 연도 조회
    public List<GradeDto> readGradeYearByStudentId(Long studentId) {

        List<StuSub> stuSubs =
                stuSubRepository.findByStudent_IdOrderBySubject_SubYearDescSubject_SemesterDesc(studentId);

        // 연도만 추출해 중복 제거
        List<Long> years = stuSubs.stream()
                .map(ss -> ss.getSubject().getSubYear())
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        List<GradeDto> result = new ArrayList<>();
        for (Long year : years) {
            GradeDto dto = new GradeDto();
            dto.setSubYear(year);
            result.add(dto);
        }

        return result;
    }

    // 학생이 수강 신청한 학기 조회
    public List<GradeDto> readGradeSemesterByStudentId(Long studentId) {

        List<StuSub> stuSubs = stuSubRepository.findByStudent_IdOrderBySubject_SubYearDescSubject_SemesterDesc(studentId);

        // 연도, 학기 중복 제거
        Set<String> seen = new HashSet<>();
        List<GradeDto> result = new ArrayList<>();

        for (StuSub ss : stuSubs) {
            Subject subject = ss.getSubject();
            if (subject == null) continue;

            Long year = subject.getSubYear();
            Long semester = subject.getSemester();
            if (year == null || semester == null) continue;

            String key = year + "-" + semester;
            if (seen.add(key)) {
                GradeDto dto = new GradeDto();
                dto.setSubYear(year);
                dto.setSemester(semester);
                result.add(dto);
            }
        }
        return result;
    }

    // 금학기 성적 조회
    public List<GradeDto> readThisSemesterByStudentId(Long studentId) {

        Long currentYear = Long.valueOf(Define.CURRENT_YEAR);
        Long currentSemester = Long.valueOf(Define.CURRENT_SEMESTER);

        List<StuSub> stuSubs = stuSubRepository.findByStudent_IdAndSubject_SubYearAndSubject_Semester(studentId, currentYear, currentSemester);

        return stuSubs.stream()
                .map(this::toGradeDto)
                .collect(Collectors.toList());

    }

    // 금학기 누계성적 조회
    public MyGradeDto readMyGradeByStudentId(Long studentId) {
        Long currentYear = Long.valueOf(Define.CURRENT_YEAR);
        Long currentSemester = Long.valueOf(Define.CURRENT_SEMESTER);

        List<StuSub> stuSubs = stuSubRepository.findByStudent_IdAndSubject_SubYearAndSubject_Semester(studentId, currentYear, currentSemester);

        if(stuSubs.isEmpty()){
            return null;
        }

        return calculateMyGradeDto(studentId, currentYear,currentSemester, stuSubs);
    }

    // 전체 누계 성적 조회
    public List<MyGradeDto> readgradeinquiryList(Long studentId) {

        List<StuSub> stuSubs =
                stuSubRepository.findByStudent_IdOrderBySubject_SubYearDescSubject_SemesterDesc(studentId);

        if (stuSubs.isEmpty()) {
            return Collections.emptyList();
        }

        //연도, 학기별 그룹
        Map<String, List<StuSub>> grouped = stuSubs.stream()
                .filter(ss -> ss.getSubject() != null)
                .filter(ss -> ss.getSubject().getSubYear() != null && ss.getSubject().getSemester() != null)
                .collect(Collectors.groupingBy(
                        ss -> ss.getSubject().getSubYear()+"-"+ss.getSubject().getSemester()
                ));

        List<MyGradeDto> result = new ArrayList<>();
        for(List<StuSub> list : grouped.values()){
            Subject subject = new Subject();
            Long year = subject.getSubYear();
            Long semester = subject.getSemester();
            result.add(calculateMyGradeDto(studentId, year, semester, list));
        }

        return result;

    }

    // 학기별 성적 조회 (전체 조회)
    public List<GradeDto> readAllGradeByStudentId(Long studentId) {

        List<StuSub> stuSubs = stuSubRepository.findByStudent_Id(studentId);

        return stuSubs.stream()
                .map(this::toGradeDto)
                .collect(Collectors.toList());
    }
    
    // 학기별 성적 조회 (선택 조회 - type 필터)
    public List<GradeDto> readGradeByType(Long studentId, Long subYear, Long semester, String type) {

       List<StuSub> stuSubs = stuSubRepository.findByStudent_IdAndSubject_SubYearAndSubject_SemesterAndSubject_Type(studentId, subYear, semester, type);

       return stuSubs.stream()
               .map(this::toGradeDto)
               .collect(Collectors.toList());

    }

    // 학기별  성적 조회 (type 무시)
    public List<GradeDto> readGradeByStudentId(Long studentId, Long subYear, Long semester) {

        List<StuSub> stuSubs =
                stuSubRepository.findByStudent_IdAndSubject_SubYearAndSubject_Semester(
                        studentId, subYear, semester
                );

        return stuSubs.stream()
                .map(this::toGradeDto)
                .collect(Collectors.toList());
    }

    // 장학금용 평균 성적 조회
    public GradeForScholarshipDto readAvgGrade(Long studentId, Long subYear, Long semester) {

        List<StuSub> stuSubs = stuSubRepository.findByStudent_IdAndSubject_SubYearAndSubject_Semester(studentId, subYear, semester);

        if(stuSubs.isEmpty()) {
            return null;
        }

        double totalWeighted = 0.0; // 학점 * 등급값 합
        double totalGrades = 0.0; // 학점 합

        for(StuSub ss : stuSubs){
            Subject subject = ss.getSubject();
            Grade grade = ss.getGrade();

            if(subject != null || grade != null) continue;

            Long subjectGrades = subject.getGrades();
            Long gradeValue = grade.getGradeValue();

            if(subjectGrades != null || gradeValue != null) continue;

            totalWeighted += subjectGrades * gradeValue;
            totalGrades += subjectGrades;

        }

        double avg = (totalGrades == 0 ) ? 0.0 : totalWeighted / totalGrades;

        GradeForScholarshipDto dto = new GradeForScholarshipDto();
        dto.setStudentId(studentId);
        dto.setSubYear(subYear);
        dto.setSemester(semester);
        dto.setAvgGrade(avg);

        return dto;

    }

    // 헬퍼 : StuSub -> GradeDto 변환
    private GradeDto toGradeDto(StuSub ss) {

        GradeDto dto = new GradeDto();

        Subject subject = ss.getSubject();
        Grade grade = ss.getGrade();

        if (subject != null) {
            dto.setSubYear(subject.getSubYear());
            dto.setSemester(subject.getSemester());
            dto.setSubjectId(subject.getId());
            dto.setName(subject.getName());
            dto.setType(subject.getType());
            if (subject.getGrades() != null) {
                dto.setGrades(String.valueOf(subject.getGrades())); // 이수 학점
            }
        }

        if (grade != null) {
            dto.setGrade(grade.getGrade()); // "A+", "B0"
            if (grade.getGradeValue() != null) {
                dto.setGradeValue(String.valueOf(grade.getGradeValue())); // "4", "3" 등
            }
        }

        // evaluationId는 현재 엔티티에 없으니 일단 null
        dto.setEvaluationId(null);

        return dto;
    }
    
    // 헬퍼 : 누계 성적(MyGradeDto) 계산
    private MyGradeDto calculateMyGradeDto(Long studentId, Long year, Long semester, List<StuSub> stuSubs) {

        MyGradeDto dto = new MyGradeDto();
        dto.setStudentId(studentId);
        dto.setSubYear(year);
        dto.setSemester(semester);

        long sumGrades = 0;  // 이수해야 할 학점(시도 학점)
        long myGrades = 0;   // 실제 이수 학점

        double totalWeighted = 0.0;
        double totalGrades = 0.0;

        for (StuSub ss : stuSubs) {
            Subject subject = ss.getSubject();
            Grade grade = ss.getGrade();
            if (subject == null) continue;

            Long subjectGrades = subject.getGrades(); // 과목 학점 수
            if (subjectGrades == null) subjectGrades = 0L;

            // 전체 시도 학점
            sumGrades += subjectGrades;

            // 성적이 있는 과목만 "이수"로 간주
            if (grade != null && grade.getGradeValue() != null) {
                myGrades += subjectGrades;
                totalWeighted += subjectGrades * grade.getGradeValue();
                totalGrades += subjectGrades;
            }
        }

        dto.setSumGrades(sumGrades);
        dto.setMyGrades(myGrades);

        float avg = (totalGrades == 0) ? 0.0f : (float) (totalWeighted / totalGrades);
        dto.setAverage(avg);

        return dto;
    }
}
