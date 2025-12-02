package com.green.university.service;

import com.green.university.dto.ProfessorListForm;
import com.green.university.dto.SyllaBusFormDto;
import com.green.university.dto.UpdateStudentGradeDto;
import com.green.university.dto.response.ReadSyllabusDto;
import com.green.university.dto.response.StudentInfoForProfessorDto;
import com.green.university.dto.response.SubjectForProfessorDto;
import com.green.university.dto.response.SubjectPeriodForProfessorDto;
import com.green.university.entity.SyllaBus;
import com.green.university.handler.exception.CustomRestfullException;
import com.green.university.repository.interfaces.*;
import com.green.university.entity.Professor;
import com.green.university.entity.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 
 * @author 김지현
 */
@Service
public class ProfessorService {

	@Autowired
	private SubjectRepository subjectRepository;
	@Autowired
	private StuSubRepository stuSubRepository;
	@Autowired
	private StuSubDetailRepository stuSubDetailRepository;
	@Autowired
	private SyllaBusRepository syllaBusRepository;
	@Autowired
	private ProfessorRepository professorRepository;

	/**
	 * 교수가 맡은 과목들의 학기 검색
	 * 
	 * @param professorId
	 * @return SubjectPeriodForProfessorDto list
	 */
	@Transactional
	public List<SubjectPeriodForProfessorDto> selectSemester(Long professorId) {
		List<Subject> subjectList = subjectRepository.findByProfessor_Id(professorId);
		// 중복 subYear, semester 있을 수 있으니 distinct 처리하려면 Set 같은 별도 로직 필요할 수 있음
		// 여기선 리스트 전체를 dto로 변환해 반환하는 예제임
		return subjectList.stream()
				.map(subject -> {
					SubjectPeriodForProfessorDto dto = new SubjectPeriodForProfessorDto();
					dto.setId(professorId);
					dto.setSubYear(subject.getSubYear());
					dto.setSemester(subject.getSemester());
					return dto;
				})
				.collect(Collectors.toList());
	}

	/**
	 * 년도와 학기, 교수 id를 이용하여 해당 과목의 정보 불러오기
	 * 
	 * @param subjectPeriodForProfessorDto
	 * @return SubjectForProfessorDto list
	 */
	@Transactional
	public List<SubjectForProfessorDto> selectSubjectBySemester(
			SubjectPeriodForProfessorDto subjectPeriodForProfessorDto) {
		List<Subject> list = subjectRepository.findByProfessor_IdAndSubYearAndSemester(subjectPeriodForProfessorDto.getId(), subjectPeriodForProfessorDto.getSemester(), subjectPeriodForProfessorDto.getSubYear());
		return list.stream()
				.map(subject -> {
					SubjectForProfessorDto subjectDto = new SubjectForProfessorDto();
					subjectDto.setId(subject.getId());
					subjectDto.setName(subject.getName());
					subjectDto.setSubDay(subject.getSubDay());
					subjectDto.setStartTime(subject.getStartTime());
					subjectDto.setEndTime(subject.getEndTime());
					subjectDto.setRoomId(subject.getRoom().getId());
					return subjectDto;
				})
				.collect(Collectors.toList());
	}

	/**
	 * 해당 과목을 듣는 학생의 세부정보 리스트로 불러오기
	 * 
	 * @param subjectId
	 * @return StudentInfoForProfessorDto list
	 */
	@Transactional
	public List<StudentInfoForProfessorDto> selectBySubjectId(Long subjectId) {
		List<StudentInfoForProfessorDto> list = stuSubRepository.selectBySubjectId(subjectId);

		return list;
	}

	/**
	 * 과목 id로 과목 Entity 불러오기
	 * 
	 * @param id
	 * @return
	 */
	@Transactional
	public Subject selectSubjectById(Long id) {
		Subject subjectEntity = subjectRepository.findById(id).orElseThrow(
				() -> new CustomRestfullException("해당 과목을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
		);;

		return subjectEntity;
	}

	/**
	 * 출결 및 성적 기입
	 * 
	 * @param updateStudentGradeDto
	 */
	@Transactional
	public void updateGrade(UpdateStudentGradeDto updateStudentGradeDto) {

		Long resultRowCount = stuSubDetailRepository.updateGrade(updateStudentGradeDto);

		if (resultRowCount != 1) {
			throw new CustomRestfullException("요청을 처리하지 못했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
		}

		resultRowCount = stuSubRepository.updateGradeByStudentIdAndSubjectId(updateStudentGradeDto);
		if (resultRowCount != 1) {
			throw new CustomRestfullException("요청을 처리하지 못했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	/**
	 * 교수 강의계획서 조회 (수정 시에도 필요)
	 * 
	 * @param subjectId
	 * @return 강의계획서
	 */
	@Transactional
	public ReadSyllabusDto readSyllabus(Long subjectId) {
		// Subject로 찾은 후 이걸로 SyllaBus도 찾고, Professor도 찾기
		Subject subject = subjectRepository.findById(subjectId).orElseThrow(
				() -> new CustomRestfullException("과목을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
		);
		SyllaBus syllaBus = syllaBusRepository.findBySubject_Id(subjectId).orElseThrow(
				() -> new CustomRestfullException("강의 계획서를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
		Professor professor = subject.getProfessor();
		if (professor == null) {
			throw new CustomRestfullException("교수 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
		}
		ReadSyllabusDto dto = new ReadSyllabusDto();

		// 교과목 정보
		dto.setSubjectId(subject.getId());
		dto.setName(subject.getName());
		dto.setSubYear(subject.getSubYear().toString());
		dto.setSemester(subject.getSemester().toString());
		dto.setGrades(subject.getGrades());
		dto.setType(subject.getType());

		dto.setSubDay(subject.getSubDay());
		dto.setStartTime(subject.getStartTime());
		dto.setEndTime(subject.getEndTime());
		dto.setRoomId(subject.getRoom().getId());
		dto.setDeptName(subject.getDepartment().getName());
		dto.setCollegeName(subject.getDepartment().getCollege().getName()); // 확인 필요

		// 교강사 정보
		dto.setProfessorName(professor.getName());
		dto.setTel(professor.getTel());
		dto.setEmail(professor.getEmail());

		// SyllaBus 부분 (개요, 목표, 정보, 계획)
		dto.setOverview(syllaBus.getOverview());
		dto.setObjective(syllaBus.getObjective());
		dto.setTextbook(syllaBus.getTextbook());
		dto.setProgram(syllaBus.getProgram());

		System.out.println(dto);
		return dto;
	}

	/**
	 * 강의 계획서 업데이트
	 * 
	 * @param syllaBusFormDto
	 */
	@Transactional
	public void updateSyllabus(SyllaBusFormDto syllaBusFormDto) {

		Long resultRowCount = syllaBusRepository.updateSyllabus(syllaBusFormDto);
		if (resultRowCount != 1) {
			throw new CustomRestfullException("제출 실패", HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	/**
	 * @param professorListForm
	 * @return 교수 리스트 조회
	 */

    @Transactional
    public List<Professor> readProfessorList(ProfessorListForm form) {

        // 1) professorId로 단일 검색 (PK는 유니크라 단건 조회가 맞음)
        if (form.getProfessorId() != null) {

            Professor p = professorRepository.findById(form.getProfessorId())
                    .orElse(null);

            return (p == null) ? List.of() : List.of(p);  // 조회 결과가 1개이므로 단건 리스트로 반환,
            // 컨트롤러에서 리스트 요구해서 리스트로 반환했음
        }

        // 2) deptId로 검색 (특정 학과 교수 목록 조회)
        if (form.getDeptId() != null) {
            return professorRepository.findByDepartment_Id(form.getDeptId());
        }

        // 3) 조건 없음 → 전체 교수 목록 조회
        return professorRepository.findAll();
    }


	/**
	 * 
	 * @param studentListForm
	 * @return 교수 수
	 */
	@Transactional
	public Long readProfessorAmount(ProfessorListForm professorListForm) {

		Long amount = null;
		if (professorListForm.getDeptId() != null) {
			amount = professorRepository.countByDepartment_Id(professorListForm.getDeptId());
		} else {
			amount = professorRepository.count(); // .count() -> 테이블의 전체 row 개수를 long 타입으로 반환하는 메서드
		}

		return amount;
	}

}
