package com.green.university.controller;

import com.green.university.dto.response.StuStatDto;
import com.green.university.dto.response.StudentDto;
import com.green.university.handler.exception.CustomRestfullException;
import com.green.university.entity.BreakApp;
import com.green.university.entity.Tuition;
import com.green.university.config.security.CustomUserDetails;
import com.green.university.service.*;
import com.green.university.utils.Define;
import com.green.university.utils.StuStatUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 
 * @author 서영 
 * 등록금, 장학금 관련
 *
 */

@RestController
@RequestMapping("/tuition")
public class TuitionController {

	@Autowired
	private HttpSession session;

	@Autowired
	private TuitionService tuitionService;

	@Autowired
	private StuStatService stuStatService;

	@Autowired
	private UserService userService;

	@Autowired
	private CollegeService collegeService;

	@Autowired
	private BreakAppService breakAppService;

	/**
	 * @return 납부된 등록금 내역 조회 페이지
	 */
	@GetMapping("/list")
	public ResponseEntity<?> tuitionList(@AuthenticationPrincipal CustomUserDetails principal) {

        Long studentId = principal.getId();
		List<Tuition> tuitionList = tuitionService.readTuitionListByStatus(studentId, true);

        return ResponseEntity.ok(Map.of(
                "tuitionList", tuitionList
        ));
	}

	/**
	 * @return 등록금 납부 고지서 조회 페이지
	 * 
	 *         해당 학기 (2023-1)에 등록금을 납부한 기록이 있다면 납부하기 버튼 제거
	 */
	@GetMapping("/payment")
	public ResponseEntity<?> tuitionPayment(@AuthenticationPrincipal CustomUserDetails principal) {

		Long studentId = principal.getId();;
        StudentDto studentInfo = userService.readStudent(studentId);
		// 등록금 납부 대상이 아니라면 진입 불가

		// 해당 학생의 학적 상태가 '졸업' 또는 '자퇴'라면 X
		// 해당 학생이 이번 학기 휴학을 승인받은 상태라면 X

		StuStatDto stuStatEntity = stuStatService.readCurrentStatus(studentInfo.getId());
		List<BreakApp> breakAppList = breakAppService.readByStudentId(studentInfo.getId()); // 최근 순으로 정렬되어 있음

		StuStatUtil.checkStuStat("등록금", stuStatEntity, breakAppList);

		// 학과 이름
		String deptName = collegeService.readDeptById(studentInfo.getDepartment().getId()).getName();

		// 단과대 이름
		String collName = collegeService
				.readCollById(collegeService.readDeptById(studentInfo.getDepartment().getId()).getCollege().getId()).getName();

		// principal.getId()를 매개변수로
		Tuition tuitionEntity = tuitionService.readByStudentIdAndSemester(principal.getId(), Define.CURRENT_YEAR,
				Define.CURRENT_SEMESTER);

		// 등록금 고지서가 생성되어 있지 않다면 들어올 수 없음
		if (tuitionEntity == null) {
			throw new CustomRestfullException("등록금 납부 기간이 아닙니다.", HttpStatus.BAD_REQUEST);
		}

        return ResponseEntity.ok(Map.of(
                "student", studentInfo,
                "deptName", deptName,
                "collName", collName,
                "tuition", tuitionEntity
        ));
	}

	/**
	 * 등록금 납부
	 * 
	 * @return 등록금 납부 페이지로 다시 돌아가서 납부 완료됨을 보여주기
	 */
	@PostMapping("/payment")
	public ResponseEntity<?> tuitionPaymentProc(@AuthenticationPrincipal CustomUserDetails principal) {

		Long studentId = principal.getId();
		tuitionService.updateStatus(studentId);

        return ResponseEntity.ok().body("등록금 납부가 정상적으로 처리되었습니다.");
    }

	/**
	 * 장학금 유형 설정 + 등록금 납부 고지서 생성 페이지
	 */
	@GetMapping("/bill")
	public ResponseEntity<?> createPayment() {

        return ResponseEntity.ok().body("등록금 납부 고지서 생성 페이지.");
	}

	/**
	 * 등록금 납부 고지서 생성 (학생 id를 가지고 와서 for문으로 돌려서 tuition을 생성하는 것 같은데)
	 */
	@GetMapping("/create")
	public ResponseEntity<?> createTuiProc() {

		List<Long> studentIdList = stuStatService.readIdList();

		// 고지서 생성 개수 반환
		Long insertCount = 0L;

		// 모든 학생에 대해 일괄 생성 (고지서 생성 대상인지는 서비스에서 확인)
		for (Long studentId : studentIdList) {
			// 생성될 때마다 +1됨
			insertCount += tuitionService.createTuition(studentId);
		}

        return ResponseEntity.ok(Map.of(
                "insertCount", insertCount
        ));
	}

}
