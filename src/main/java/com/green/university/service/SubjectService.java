package com.green.university.service;

import com.green.university.dto.AllSubjectSearchFormDto;
import com.green.university.dto.CurrentSemesterSubjectSearchFormDto;
import com.green.university.dto.response.SubjectDto;
import com.green.university.exception.CustomRestfullException;
import com.green.university.repository.interfaces.SubjectRepository;
import com.green.university.entity.Subject;
import com.green.university.utils.Define;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 서영
 */

@Service
public class SubjectService {

    @Autowired
    private SubjectRepository subjectRepository;

    /**
     * @return 전체 강의 조회에 사용할 강의 정보 (학생용) 전체 연도-학기에 해당하는 강의가 출력됨
     */
    @Transactional
    public List<SubjectDto> readSubjectList() {
        List<Subject> subjectList = subjectRepository.findAll();

        // 엔티티를 dto로 변환시키기
        return subjectList.stream()
                .map(subject -> {
                    SubjectDto dto = new SubjectDto();

                    dto.setId(subject.getId());
                    dto.setName(subject.getName());

                    if(subject.getProfessor() != null) {
                        dto.setProfessorId(subject.getProfessor().getId());
                        dto.setProfessorName(subject.getProfessor().getName());
                    }

                    if(subject.getRoom() != null) {
                        dto.setRoomId(subject.getRoom().getId());
                    }

                    if(subject.getDepartment() != null) {
                        dto.setDeptId(subject.getDepartment().getId());
                        dto.setDeptName(subject.getDepartment().getName());
                    }

                    dto.setType(subject.getType());
                    dto.setSubYear(subject.getSubYear());
                    dto.setSemester(subject.getSemester());
                    dto.setSubDay(subject.getSubDay());
                    dto.setStartTime(subject.getStartTime());
                    dto.setEndTime(subject.getEndTime());
                    dto.setGrades(subject.getGrades());
                    dto.setCapacity(subject.getCapacity());
                    dto.setNumOfStudent(subject.getNumOfStudent());

                    dto.setStatus(false); // 신청 여부는 기본 false, 나중에 로직에 변경 예정

                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * @return 전체 강의 조회에 사용할 강의 정보 (학생용) 전체 연도-학기에 해당하는 강의가 출력됨 + 페이징 처리
     */
    @Transactional
    public Page<SubjectDto> readSubjectListPage(Integer page) {
        Pageable pageable = PageRequest.of(page, Define.SUBJECT_PAGE_SIZE);
        Page<Subject> subjectPage = subjectRepository.findAll(pageable);
        // 엔티티를 dto로 변환시키기 (Page라서 map 이용) -> 이때 null 처리 안해도 되나요 ..?
        return subjectPage.map(SubjectDto::fromEntity);
    }

    /**
     * @param allSubjectSearchFormDto
     * @return 전체 강의 목록에서 필터링할 때 출력할 강의
     */
    @Transactional
    public List<SubjectDto> readSubjectListSearch(AllSubjectSearchFormDto allSubjectSearchFormDto) {

        List<Subject> subjectList = subjectRepository.findBySubYearAndSemesterAndDepartment_IdAndNameContaining(
                allSubjectSearchFormDto.getSubYear(), allSubjectSearchFormDto.getSemester(), allSubjectSearchFormDto.getDeptId(), allSubjectSearchFormDto.getName());
        return subjectList.stream()
                .map(SubjectDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * @return 수강 신청에 사용할 강의 정보 (학생용) 현재 연도-학기에 해당하는 강의만 출력됨
     */
    @Transactional
    public List<SubjectDto> readSubjectListByCurrentSemester() {
        List<Subject> subjectList = subjectRepository.findBySubYearAndSemester(
                Define.CURRENT_YEAR, Define.CURRENT_SEMESTER);
        return subjectList.stream()
                .map(subject -> {
                    SubjectDto dto = new SubjectDto();
                    dto.setId(subject.getId());
                    dto.setName(subject.getName());
                    if(subject.getProfessor() != null) {
                        dto.setProfessorId(subject.getProfessor().getId());
                        dto.setProfessorName(subject.getProfessor().getName());
                    }
                    if(subject.getRoom() != null) {
                        dto.setRoomId(subject.getRoom().getId());
                    }
                    if(subject.getDepartment() != null) {
                        dto.setDeptId(subject.getDepartment().getId());
                        dto.setDeptName(subject.getDepartment().getName());
                    }
                    dto.setType(subject.getType());
                    dto.setSubYear(subject.getSubYear());
                    dto.setSubDay(subject.getSubDay());
                    dto.setStartTime(subject.getStartTime());
                    dto.setEndTime(subject.getEndTime());
                    dto.setGrades(subject.getGrades());
                    dto.setCapacity(subject.getCapacity());
                    dto.setNumOfStudent(subject.getNumOfStudent());
                    dto.setStatus(false);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * @return 수강 신청에 사용할 강의 정보 (학생용) 현재 연도-학기에 해당하는 강의만 출력됨 + 페이징 처리
     */
    @Transactional
    public Page<SubjectDto> readSubjectListByCurrentSemesterPage(Integer page) {
        Pageable pageable = PageRequest.of(page, Define.SUBJECT_PAGE_SIZE);
        Page<Subject> subjectPage = subjectRepository.findBySubYearAndSemester(Define.CURRENT_YEAR,
                Define.CURRENT_SEMESTER, pageable);
        return subjectPage.map(SubjectDto::fromEntity);
    }

    /**
     * @return 강의 시간표에서 필터링할 때 출력할 강의
     */
    @Transactional
    public List<SubjectDto> readSubjectListSearchByCurrentSemester(CurrentSemesterSubjectSearchFormDto dto) {

        List<Subject> subjectList = subjectRepository.findBySubYearAndSemesterAndTypeAndDepartment_IdAndNameContaining(
                dto.getSubYear(), dto.getSemester(), dto.getType(), dto.getDeptId(), dto.getName());
        return subjectList.stream()
                .map(subject -> {
                    SubjectDto subjectDto = new SubjectDto();
                    subjectDto.setId(subject.getId());
                    subjectDto.setName(subject.getName());
                    if(subject.getProfessor() != null) {
                        subjectDto.setProfessorId(subject.getProfessor().getId());
                        subjectDto.setProfessorName(subject.getProfessor().getName());
                    }
                    if(subject.getRoom() != null) {
                        subjectDto.setRoomId(subject.getRoom().getId());
                    }
                    if(subject.getDepartment() != null) {
                        subjectDto.setDeptId(subject.getDepartment().getId());
                    }
                    subjectDto.setType(subject.getType());
                    subjectDto.setSubYear(subject.getSubYear());
                    subjectDto.setSubDay(subject.getSubDay());
                    subjectDto.setStartTime(subject.getStartTime());
                    subjectDto.setEndTime(subject.getEndTime());
                    subjectDto.setGrades(subject.getGrades());
                    subjectDto.setCapacity(subject.getCapacity());
                    subjectDto.setNumOfStudent(subject.getNumOfStudent());
                    subjectDto.setStatus(false);
                    return subjectDto;
                })
                .collect(Collectors.toList());

    }

    /**
     * 현재 인원을 1명 추가함
     */
    @Transactional
    public void updatePlusNumOfStudent(Long id) {
        Subject subject =  subjectRepository.findById(id)
                .orElseThrow(() -> new CustomRestfullException("해당 과목을 찾을 수 없습니다.",HttpStatus.NOT_FOUND, "/break/appList"));

        Long current = subject.getNumOfStudent();
        if(current == null) {
            current = 0L;
        }

        subject.setNumOfStudent(current + 1L);
    }

    /**
     * 현재 인원을 1명 삭제함
     */
    @Transactional
    public void updateMinusNumOfStudent(Long id) {
        Subject subject =  subjectRepository.findById(id)
                .orElseThrow(() -> new CustomRestfullException("해당 과목을 찾을 수 없습니다.",HttpStatus.NOT_FOUND, "/break/appList"));

        Long current = subject.getNumOfStudent();
        if(current == null || current <= 0){
            subject.setNumOfStudent(0L);  // 음수 방지
        } else {
            subject.setNumOfStudent(current-1L);
        }
    }

    @Transactional
    public Subject readBySubjectId(Long id) {
        Subject subjectEntity = subjectRepository.findById(id).orElseThrow(
                () -> new CustomRestfullException("해당 과목을 찾을 수 없습니다.", HttpStatus.NOT_FOUND, "/break/appList")
        );;
        return subjectEntity;
    }

}
