package com.green.university.service;

import com.green.university.dto.SyllaBusFormDto;
import com.green.university.dto.UpdateStudentGradeDto;
import com.green.university.dto.response.*;
import com.green.university.entity.*;
import com.green.university.exception.CustomRestfullException;
import com.green.university.repository.*;
import com.green.university.specification.ProfessorSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private static final int PAGE_SIZE = 20; // 교수 리스트 / 검색 페이징 용
    @Autowired
    private GradeRepository gradeRepository;

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
		List<Subject> list = subjectRepository.findByProfessor_IdAndSubYearAndSemester(subjectPeriodForProfessorDto.getId(), subjectPeriodForProfessorDto.getSubYear(), subjectPeriodForProfessorDto.getSemester());
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
	 * 해당 과목을 듣는 학생의 세부정보 리스트로 불러오기 (교수 확인용)
	 * 
	 * @param subjectId
	 * @return StudentInfoForProfessorDto list
	 */
	@Transactional
	public List<StudentInfoForProfessorDto> selectBySubjectId(Long subjectId) {
		return stuSubRepository.findBySubject_Id(subjectId)
				.stream()
				.map(StudentInfoForProfessorDto::fromEntity)  // 각 StuSub → DTO 변환
				.collect(Collectors.toList());
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
		);

		return subjectEntity;
	}

	/**
	 * 출결 및 성적 기입
	 * 
	 * @param updateStudentGradeDto
	 */
    /**
     * 출결 및 성적 기입
     */
    @Transactional
    public void updateGrade(Long subjectId, Long studentId, UpdateStudentGradeDto dto) {

        // 1. StuSub 찾기
        StuSub stuSub = stuSubRepository
                .findByStudent_IdAndSubject_Id(studentId, subjectId)
                .orElseThrow(() -> new RuntimeException("학생 과목 정보 없음"));

        // 2. StuSubDetail 찾기
        StuSubDetail detail = stuSubDetailRepository.findByStuSub(stuSub)
                .orElseThrow(() -> new RuntimeException("출결 정보 없음"));

        // 3. 출결/점수 업데이트
        detail.setAbsent(dto.getAbsent());
        detail.setLateness(dto.getLateness());
        detail.setHomework(dto.getHomework());
        detail.setMildExam(dto.getMidExam());  // 컬럼명 mildExam 맞음
        detail.setFinalExam(dto.getFinalExam());
        detail.setConvertedMark(dto.getConvertedMark());

        // 4. 등급 엔티티 조회
        Grade grade = gradeRepository.findByGrade(dto.getGrade())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 학점 등급입니다: " + dto.getGrade()));
        stuSub.setGrade(grade);

        // 5. 완성학점 업데이트 (점수 기준)
        stuSub.setCompleteGrade(dto.getConvertedMark());

        // 6. 저장
        stuSubDetailRepository.save(detail);
        stuSubRepository.save(stuSub);
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
		Syllabus syllabus = syllaBusRepository.findBySubject_Id(subjectId).orElseThrow(
				() -> new CustomRestfullException("강의 계획서를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
		Professor professor = subject.getProfessor();
		if (professor == null) {
			throw new CustomRestfullException("교수 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
		}
		ReadSyllabusDto dto = new ReadSyllabusDto(subject, professor, syllabus); // Dto에 생성자 추가함
		return dto;
	}

	/**
	 * 강의 계획서 업데이트
	 * 
	 * @param syllaBusFormDto
	 */
	@Transactional
	public void updateSyllabus(Long id, SyllaBusFormDto syllaBusFormDto) {

        Syllabus syllabus = syllaBusRepository.findBySubject_Id(id).orElseThrow(
                () -> new CustomRestfullException("강의 계획서를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        syllabus.setOverview(syllaBusFormDto.getOverview());
        syllabus.setObjective(syllaBusFormDto.getObjective());
        syllabus.setTextbook(syllaBusFormDto.getTextbook());
        syllabus.setProgram(syllaBusFormDto.getProgram());

        syllaBusRepository.save(syllabus);
	}

	/**
	 * @return 교수 리스트 조회 + 검색
	 */

    @Transactional
    public Page<ProfessorDto> readProfessorList(Long professorId, String deptName, Pageable pageable) {

        Specification<Professor> spec = (
                root, query, cb) -> null; // 조건 없이 전체 조회
        if (professorId != null) {
            spec = spec.and(ProfessorSpecification.hasProfessorId(professorId));
        }
        if (deptName != null) {
            spec = spec.and(ProfessorSpecification.hasDepartmentName(deptName));
        }
        if(deptName != null && professorId != null) {
            spec = spec.and(ProfessorSpecification.hasProfessorIdAndDepartmentName(professorId, deptName));
        }

        Page<Professor> Professor = professorRepository.findAll(spec, pageable);
        return Professor.map(ProfessorDto::fromEntity); // dto로 반환해주기

    }

}
