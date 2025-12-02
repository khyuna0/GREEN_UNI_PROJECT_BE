package com.green.university.service;

import com.green.university.dto.response.GradeForScholarshipDto;
import com.green.university.handler.exception.CustomRestfullException;
import com.green.university.repository.interfaces.*;
import com.green.university.entity.*;
import com.green.university.utils.Define;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author 서영
 */

@Service
public class TuitionService {

	@Autowired
	private TuitionRepository tuitionRepository;

	@Autowired
	private ScholarshipRepository scholarshipRepository;

	@Autowired
	private StuStatService stuStatService;

	@Autowired
	private BreakAppService breakAppService;

	@Autowired
	private UserService userService;

	@Autowired
	private GradeService gradeService;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private StuSchRepository stuSchRepository;

    @Autowired
    private CollTuitRepository collTuitRepository;


	/**
	 * @param studentId (principal의 id와 동일)
	 * @return 해당 학생의 모든 등록금 납부 내역 단순 조회
	 */
	@Transactional
	public List<Tuition> readTuitionList(Long studentId) { // 안쓰는데?

		List<Tuition> tuitionEntityList = tuitionRepository.findByStudent_Id(studentId);

		return tuitionEntityList;
	}

	/**
	 * @param studentId (principal의 id와 동일)
	 * @return 해당 학생의 납부 여부에 따른 등록금 납부 내역
	 */
	@Transactional
	public List<Tuition> readTuitionListByStatus(Long studentId, Boolean status) {
        // 컨트롤러에서 항상 true를 내려주기 때문에, 납부 완료된 등록금 내역 리스트만 뽑아줌
		List<Tuition> tuitionEntityList = tuitionRepository.findByStudent_IdAndStatus(studentId, status);

		return tuitionEntityList;
	}

	/**
	 * @return 해당 학생의 현재 년도, 학기별 등록금 고지서
	 */
	@Transactional
	public Tuition readByStudentIdAndSemester(Long studentId, Long tuiYear, Long semester) {

		Tuition tuitionEntity = tuitionRepository.findByStudent_IdAndTuiYearAndSemester(studentId, tuiYear, semester);

		return tuitionEntity;
	}

	/**
	 * 장학금 유형 결정
	 */
	public Long createCurrentSchType(Long studentId) {

        Student studentEntity = userService.readStudent(studentId); // 예외처리 완료된 유저 조회

		StuSch stuSch = new StuSch();
		stuSch.setStudent(studentEntity);
		stuSch.setSchYear(Define.CURRENT_YEAR);
		stuSch.setSemester(Define.CURRENT_SEMESTER);

		// 1학년 2학기 이상의 학생이라면
		if (studentEntity.getGrade() > 1 || studentEntity.getSemester() == 2) {
			// 직전 학기 성적 평균
			// 상수로 선언해둬서 노란줄 뜨는 거니까 무시하기
			GradeForScholarshipDto gradeDto = null;
			if (Define.CURRENT_SEMESTER == 1) {
				gradeDto = gradeService.readAvgGrade(studentId, Define.CURRENT_YEAR - 1, 2L);
			} else {
				gradeDto = gradeService.readAvgGrade(studentId, Define.CURRENT_YEAR, 1L);
			}

			if (gradeDto == null) {
                stuSchRepository.save(stuSch); // setSchType이 null 로 저장된다
				return null; // 학점이 없어서 장학금 지급 안됨
			} else {
				Double avgGrade = gradeDto.getAvgGrade();
				// 평점에 따라 장학금 유형 결정
				if (avgGrade >= 4.2) {
					stuSch.setSchType(scholarshipRepository.findById(1L).orElseThrow(() -> new CustomRestfullException("해당 장학금 정보가 없습니다.", HttpStatus.NOT_FOUND)));
				} else if (avgGrade >= 3.7) {
					stuSch.setSchType(scholarshipRepository.findById(2L).orElseThrow(() -> new CustomRestfullException("해당 장학금 정보가 없습니다.", HttpStatus.NOT_FOUND)));
				}
			}

			// 1학년 1학기 학생이라면
		} else {
			stuSch.setSchType(scholarshipRepository.findById(2L).orElseThrow(() -> new CustomRestfullException("해당 장학금 정보가 없습니다.", HttpStatus.NOT_FOUND)));
		}

        stuSchRepository.save(stuSch);
		return stuSch.getSchType().getType(); // 장학금 타입이 결정남
	}

	/**
	 * 등록금 고지서 생성 교직원 탭에서 사용하도록 할 것
	 * 
	 * @param studentId (principal의 id와 동일)
	 */
	@Transactional
	public Long createTuition(Long studentId) { // 고지서 생성 버튼 누르면 실행됨

		// 해당 학생의 학적 상태가 '졸업' 또는 '자퇴'라면 생성하지 않음
		StuStat stuStatEntity = stuStatService.readCurrentStatus(studentId);
		if (stuStatEntity.getStatus().equals("졸업") || stuStatEntity.getStatus().equals("자퇴")) {
			return 0L;
		}

		// 해당 학생이 현재 학기 휴학을 승인받은 상태라면 생성하지 않음
		List<BreakApp> breakAppList = breakAppService.readByStudentId(studentId); // 최근 순으로 정렬되어 있음
		for (BreakApp b : breakAppList) {
			// 휴학 신청이 승인된 상태일 때
			if (b.getStatus().equals("승인")) {
				// 휴학 종료 연도가 현재 연도보다 이후라면 생성하지 않음
				if (b.getToYear() > Define.CURRENT_YEAR) {
					return 0L;
					// 휴학 종료 연도가 현재 연도와 같을 경우
				} else if (b.getToYear() == Define.CURRENT_YEAR) {
					// 현재 학기 == 1 && 종료 학기 == 1이면 생성하지 않음
					// 현재 학기 == 1 && 종료 학기 == 2이면 생성하지 않음
					// 현재 학기 == 2 && 종료 학기 == 1이면 생성함
					// 현재 학기 == 2 && 종료 학기 == 2이면 생성하지 않음
					if (b.getToSemester() >= Define.CURRENT_SEMESTER) {
						return 0L;
					}
				}
			}
		}

		// 이미 해당 학기의 등록금 고지서가 존재한다면 생성하지 않음
		if (readByStudentIdAndSemester(studentId, Define.CURRENT_YEAR, Define.CURRENT_SEMESTER) != null) {
			return 0L;
		}

        // 전체 등록 금액 구하기
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new CustomRestfullException("학생 정보 없음", HttpStatus.NOT_FOUND));

        // 학생 정보에서 학과 정보 -> 단과대 정보 -> 단과대 별 등록금 정보 찾아 저장함
        Long tuiAmount = collTuitRepository.findById(student.getDepartment().getCollege().getId()).get().getAmount();

        // ======== 전체 등록 금액 구하기 끝

		// 장학금 유형과 금액 결정 (null이면 장학금 지원 대상이 아님)
		Long schType = createCurrentSchType(studentId);

        // 해당 학생의 특정 년도, 학기의 장학금 유형과 금액 정보 구하기
        StuSch stuSch = stuSchRepository.findByStudent_IdAndSchYearAndSemester(studentId, Define.CURRENT_YEAR, Define.CURRENT_SEMESTER);

        Long schAmount = 0L;

		// 장학금액 확인 (장학금 지원 대상이 아니면 schAmount(장학금액) 0으로 저장함)
        if(stuSch.getSchType() != null) {
            Scholarship scholarship = scholarshipRepository.findById(stuSch.getSchType().getType()).orElseThrow(() -> new CustomRestfullException("장학금 정보 없음", HttpStatus.NOT_FOUND));
            if (tuiAmount < schAmount) {
                schAmount = tuiAmount;
            } else {
                schAmount = scholarship.getMaxAmount();
            }
        }

		// 등록금 고지서 생성
		Tuition tuition = new Tuition(studentId, Define.CURRENT_YEAR, Define.CURRENT_SEMESTER, tuiAmount, schType,
				schAmount);

        tuitionRepository.save(tuition);

		// 등록금 고지서가 생성된 횟수를 출력하기 위해 반환 / 성공하면 1L, 실패 시 0L을 반환한다 -> 이후 n개의 고지서 생성 알림에서 사용됨
		return 1L;
	}

	/**
	 * 등록금 납부
	 */
	@Transactional
	public void updateStatus(Long studentId) {

		Tuition tuition = tuitionRepository.findByStudent_IdAndTuiYearAndSemester(studentId, Define.CURRENT_YEAR, Define.CURRENT_SEMESTER);
        tuition.setStatus(true);

			String status = stuStatService.readCurrentStatus(studentId).getStatus();
			if ("휴학".equals(status)) {
				stuStatService.updateStatus(studentId, "재학", "9999-01-01", null);
			}

		}
	}


