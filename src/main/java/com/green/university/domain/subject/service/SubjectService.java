package com.green.university.domain.subject.service;

import com.green.university.domain.subject.dto.AllSubjectSearchFormDto;
import com.green.university.domain.subject.dto.CurrentSemesterSubjectSearchFormDto;
import com.green.university.domain.subject.dto.SubjectDto;
import com.green.university.domain.subject.entity.Subject;
import com.green.university.domain.subject.repository.SubjectRepository;
import com.green.university.domain.subject.specification.SubjectSpecification;
import com.green.university.global.exception.CustomRestfullException;
import com.green.university.global.utils.Define;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class SubjectService {

    @Autowired
    private SubjectRepository subjectRepository;

    // 모든 강의 조회 (전체 연도, 학기) + 페이징 + 검색
    @Transactional(readOnly = true)
    public Page<SubjectDto> readSubjectList(AllSubjectSearchFormDto dto, Pageable pageable) {
        Long subYear = dto.getSubYear();
        Long semester = dto.getSemester();
        String deptName = dto.getDeptName();
        String name = dto.getName();
        String type = dto.getType();

        Specification<Subject> spec = (root, query, cb) -> null;
        // 연도, 학기
        if (subYear != null || semester != null) {
            spec = spec.and(SubjectSpecification.currentSemester(subYear, semester));
        }
        // 학과명
        if (deptName != null) {
            spec = spec.and(SubjectSpecification.hasDepartmentName(deptName));
        }
        // 강의명
        if (name != null && !name.isEmpty()) {
            spec = spec.and(SubjectSpecification.nameContains(name));
        } // 강의구분
        if (type != null && !type.isEmpty()) {
            spec = spec.and(SubjectSpecification.hasType(type));
        }

        Page<Subject> subjectList = subjectRepository.findAll(spec, pageable);

        return subjectList.map(SubjectDto::fromEntity);
    }

    // 🔥 수강 신청에 사용할 강의 정보 (학생용) 현재 연도-학기에 해당하는 강의만 출력 + 페이징 처리 + 검색
    @Transactional(readOnly = true)
    public Page<SubjectDto> readSubjectListByCurrentSemesterPage(CurrentSemesterSubjectSearchFormDto dto, Pageable pageable) {
        String type = dto.getType();
        String deptName = dto.getDeptName();
        String name = dto.getName();

        // 조건 없이 전체 조회
        Specification<Subject> spec = (root, query, cb) -> null;

        // 현재 연도, 학기에 해당하는 과목
        spec = spec.and(SubjectSpecification.currentSemester(Define.CURRENT_YEAR, Define.CURRENT_SEMESTER));
        // 전공 또는 교양
        if (type != null && !type.isEmpty()) {
            spec = spec.and(SubjectSpecification.hasType(type));
        }
        // 학과명
        if (deptName != null) {
            spec = spec.and(SubjectSpecification.hasDepartmentName(deptName));
        }
        // 강의명
        if (name != null && !name.isEmpty()) {
            spec = spec.and(SubjectSpecification.nameContains(name));
        }
        Page<Subject> subjectPage = subjectRepository.findAll(spec, pageable);
        return subjectPage.map(SubjectDto::fromEntity);
    }

    // 현재 인원을 1명 추가함
    @Transactional
    public void updatePlusNumOfStudent(Long id) {
        Subject subject = subjectRepository.findById(id).orElseThrow(
                () -> new CustomRestfullException("해당 과목을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        Long current = subject.getNumOfStudent();
        if (current == null) {
            current = 0L;
        }

        subject.setNumOfStudent(current + 1L);
    }

    // 현재 인원을 1명 삭제함
    @Transactional
    public void updateMinusNumOfStudent(Long id) {
        Subject subject = subjectRepository.findById(id).orElseThrow(
                () -> new CustomRestfullException("해당 과목을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        Long current = subject.getNumOfStudent();
        if (current == null || current <= 0) {
            subject.setNumOfStudent(0L);  // 음수 방지
        } else {
            subject.setNumOfStudent(current - 1L);
        }
    }

    // repository에서 findId로 직접 사용하면 안 되나요..?
    @Transactional
    public Subject readBySubjectId(Long id) {
        return subjectRepository.findById(id).orElseThrow(
                () -> new CustomRestfullException("해당 과목을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        );
    }

}
