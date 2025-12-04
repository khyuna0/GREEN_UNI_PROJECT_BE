package com.green.university.controller;

import com.green.university.dto.response.GradeDto;
import com.green.university.dto.response.MyGradeDto;
import com.green.university.security.CustomUserDetails;
import com.green.university.service.GradeService;
import com.green.university.utils.Define;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 
 * @author 편용림
 * 
 *         금학기,학기별 성적, 누계성적 조회
 * 
 */

@RestController
@RequestMapping("/grade")
public class GradeController {

	@Autowired
	private HttpSession session;

	@Autowired
	private GradeService gradeService;

	/**
	 * 금학기 성적조회
	 *
	 * @return
	 */
	@GetMapping("/thisSemester")
	public ResponseEntity<?> thisSemester(@AuthenticationPrincipal CustomUserDetails principal) {

        Long studentId = principal.getId();

		// 학생이 수강 신청한 연도 조회
		List<GradeDto> yearList = gradeService.readGradeYearByStudentId(principal.getId());

        List<GradeDto> thisSemester = null; // 값 보내주기 위해 선언부만 만듬
        MyGradeDto mygrade = null;

		// 수강한 연도가 없으면 금학기 성적조회 x
		if (yearList.size() != 0) {

			// 금학기 성적조회 기능
			thisSemester = gradeService.readThisSemesterByStudentId(principal.getId());

			// 누계 성적 조회
			mygrade = gradeService.readMyGradeByStudentId(principal.getId());
		}

        return ResponseEntity.ok(Map.of(
                "yearList", yearList,
                "gradeList", thisSemester, // 상태에 따라 null 일수도 있음
                "mygrade", mygrade
        ));
	}

	/**
	 * 학기별 성적조회
	 *
	 * @return
	 */
	@GetMapping("/semester")
	public ResponseEntity<?> semester() {

		PrincipalDto principal = (PrincipalDto) session.getAttribute(Define.PRINCIPAL);

		// 학생이 수강 신청한 연도 조회
		List<GradeDto> yearList = gradeService.readGradeYearByStudentId(principal.getId());
		// 전체 성적 조회
		List<GradeDto> gradeAllList = gradeService.readAllGradeByStudentId(principal.getId());
		// 학생이 신청한 학기가 있는지 찾는 기능
		List<GradeDto> semesterList = gradeService.readGradeSemesterByStudentId(principal.getId());

        return ResponseEntity.ok(Map.of(
                "yearList", yearList,
                "gradeList", gradeAllList,
                "semesterList", semesterList
        ));
	}

	/**
	 * 학기별 성적 조회 Todo 성적조회 기능 정리하기
	 *
	 * @param httpServletRequest
	 * @return
	 */
	@PostMapping("/read")
	public ResponseEntity<?> readGradeProc(HttpServletRequest httpServletRequest) {

		PrincipalDto principal = (PrincipalDto) session.getAttribute(Define.PRINCIPAL);

		// 학생이 수강 신청한 연도 조회
		List<GradeDto> yearList = gradeService.readGradeYearByStudentId(principal.getId());

		// 학생이 수강 신청한 학기 조회
		List<GradeDto> semesterList = gradeService.readGradeSemesterByStudentId(principal.getId());

		// 조회 할때 값을 들고옴
		String type = httpServletRequest.getParameter("type"); // 파라미터로 받는데... 따로 조회용 DTO를 만들어야 할까? (보류)
		Long subYear = Long.valueOf(httpServletRequest.getParameter("subYear"));
		Long sesmeter = Long.valueOf(httpServletRequest.getParameter("sesmeter"));

		if (type.equals("전체")) {
			List<GradeDto> gradeAllList = gradeService.readGradeByStudentId(principal.getId(), subYear, sesmeter);
			model.addAttribute("gradeList", gradeAllList);
		} else {
			List<GradeDto> gradeList = gradeService.readGradeByType(principal.getId(), subYear, sesmeter, type);
			model.addAttribute("gradeList", gradeList);
		}

		model.addAttribute("yearList", yearList);
		model.addAttribute("semesterList", semesterList);

        return ResponseEntity.ok(Map.of(
                "yearList", yearList,
                "gradeList", gradeAllList,
                "semesterList", semesterList
        ));
	};

	/**
	 * 총 누계성적 조회
	 *
	 * @return
	 */
	@GetMapping("total")
	public ResponseEntity<?> totalGrade() {

		PrincipalDto principal = (PrincipalDto) session.getAttribute(Define.PRINCIPAL);

		// 학생이 수강 신청한 연도 조회
		List<GradeDto> yearList = gradeService.readGradeYearByStudentId(principal.getId());
		List<MyGradeDto> mygradeList = gradeService.readgradeinquiryList(principal.getId());

        return ResponseEntity.ok(Map.of(
                "yearList", yearList,
                "mygradeList", mygradeList
        ));
	}

}
