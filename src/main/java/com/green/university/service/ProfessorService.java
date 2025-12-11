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

		return stuSubDetailRepository.findBySubject_Id(subjectId)
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

        // 3. 해당 강의 듣는 학생 수 계산 (상대평가 계산 용 / 수강 인원이 10명 이하면 절대평가) todo 기준 픽스하기
        Long numOfStudent = subjectRepository.findNumOfStudentById(subjectId);

        // 4. 기본 성적 입력
        detail.setAbsent(dto.getAbsent()); // 결석
        detail.setLateness(dto.getLateness()); // 지각
        detail.setHomework(dto.getHomework()); // 과제점수
        detail.setMildExam(dto.getMidExam()); // 중간
        detail.setFinalExam(dto.getFinalExam()); // 기말


        // 5. 환산점수 계산

//        환산점수 계산 기준
//        - 감점 요소
//        지각 3회 = 결석 1회
//        결석 1회 = -2 점 (결석 4회 F)
//
//        - 환산점수 계산
//        과제 점수 20%
//        중간 시험 30%
//        기말 시험 50%
//
//        최종 환산점수 = 환산점수 - 감점 요소
//        전체 수강인원 20명 이상일 때 - 상대평가 등급 자동 산출
//        등급은 처음 자동 산출, 이후 교수 수정 가능

        // 출석 감점 계산
        long latenessToAbsent = dto.getLateness() / 3; // 지각 3회 - 결석 1번
        long totalAbsent = dto.getAbsent() + latenessToAbsent;
        double penalty = totalAbsent * 2;

        // 환산점수 계산
        double convertedmark =
                ( dto.getHomework() * 0.2 ) + // 과제 점수 20%
                ( dto.getMidExam() * 0.3 ) + // 중간 점수 30%
                ( dto.getFinalExam() * 0.5 ); // 기말 점수 50%

        // 최종 환산점수 계산 첫째 자리까지 반올림 (환산점수 - 감점)
        double finalConvertedMark = Math.round((convertedmark - penalty) * 10) / 10.0;

        // 5. 계산된 환산점수
        detail.setConvertedMark(finalConvertedMark);
        stuSub.setCompleteGrade(finalConvertedMark);

        stuSubDetailRepository.save(detail);
        stuSubRepository.save(stuSub);

        // 6. 등급 계산
        // 수강 인원이 20명 이상이면 상대평가, 미만이면 절대평가
        String  gradeValue = "F"; // 기본 값
        if(totalAbsent < 5 && numOfStudent < 20) { // 절대평가 기준
            if (finalConvertedMark >= 95) gradeValue = "A+";
            else if (finalConvertedMark >= 90) gradeValue = "A0";
            else if (finalConvertedMark >= 85) gradeValue = "B+";
            else if (finalConvertedMark >= 80) gradeValue = "B0";
            else if (finalConvertedMark >= 75) gradeValue = "C+";
            else if (finalConvertedMark >= 70) gradeValue = "C0";
            else if (finalConvertedMark >= 65) gradeValue = "D+";
            else if (finalConvertedMark >= 60) gradeValue = "D0";
        } else if (totalAbsent < 5) {

        }


        // 6. 등급 엔티티 조회
        Grade grade = gradeRepository.findByGrade(gradeValue)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 학점 등급입니다"));
        stuSub.setGrade(grade);
        detail.setGrade(grade.getGrade()); // 단순 출력용으로 저장함
        // 5. 완성학점 업데이트 (점수 기준)


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
