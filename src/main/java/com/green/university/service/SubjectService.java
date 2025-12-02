package com.green.university.service;

import com.green.university.dto.AllSubjectSearchFormDto;
import com.green.university.dto.CurrentSemesterSubjectSearchFormDto;
import com.green.university.dto.response.SubjectDto;
import com.green.university.handler.exception.CustomRestfullException;
import com.green.university.repository.interfaces.SubjectRepository;
import com.green.university.entity.Subject;
import com.green.university.utils.Define;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
	 * 페이징 처리
	 */
	@Transactional
	public List<SubjectDto> readSubjectListPage(Long page) {

		List<SubjectDto> subDtoList = subjectRepository.selectDtoAllLimit(page);

		return subDtoList;
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

					dto.setStatus(false); // 신청 여부 기본 false로 세팅

					return dto;
				})
				.collect(Collectors.toList());
	}

	/**
	 * @return 수강 신청에 사용할 강의 정보 (학생용) 현재 연도-학기에 해당하는 강의만 출력됨
	 */
	@Transactional
	public List<SubjectDto> readSubjectListByCurrentSemester() {

		List<SubjectDto> subDtoList = subjectRepository.selectDtoBySemester(Define.CURRENT_YEAR,
				Define.CURRENT_SEMESTER);

		return subDtoList;
	}

	/**
	 * 페이징 처리
	 */
	@Transactional
	public List<SubjectDto> readSubjectListByCurrentSemesterPage(Long page) {

		List<SubjectDto> subDtoList = subjectRepository.selectDtoBySemesterLimit(Define.CURRENT_YEAR,
				Define.CURRENT_SEMESTER, page);

		return subDtoList;
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
		Long resultRowCount = subjectRepository.updateNumOfStudent(id, "추가");
		if (resultRowCount != 1) {
			throw new CustomRestfullException("현재 인원 수정이 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * 현재 인원을 1명 삭제함
	 */
	@Transactional
	public void updateMinusNumOfStudent(Long id) {
		Long resultRowCount = subjectRepository.updateNumOfStudent(id, "삭제");
		if (resultRowCount != 1) {
			throw new CustomRestfullException("현재 인원 수정이 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@Transactional
	public Subject readBySubjectId(Long id) {
		Subject subjectEntity = subjectRepository.findById(id).orElseThrow(
				() -> new CustomRestfullException("해당 과목을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
		);;
		return subjectEntity;
	}

}
