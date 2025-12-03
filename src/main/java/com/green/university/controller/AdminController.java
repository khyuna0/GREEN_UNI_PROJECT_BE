package com.green.university.controller;

import com.green.university.dto.*;
import com.green.university.entity.College;
import com.green.university.entity.Department;
import com.green.university.entity.Room;
import com.green.university.entity.Subject;
import com.green.university.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 
 * @author 박성희 
 * Admin 수업 조회/입력 관련 Controller
 */
@RestController
@RequestMapping("/admin")
public class AdminController {
	@Autowired
	private AdminService adminService;

	/**
	 * 
	 * @return 단과대 페이지 - 단과대학 리스트 조회
	 */
	@GetMapping("/college")
	public ResponseEntity<?> college( @RequestParam(defaultValue = "select") String crud) {
		// crud는 등록/삭제 시 ?crud=insert 처럼 파라미터로 붙어 무슨 작업하는지 상태를 나타낸다
		List<College> collegeList = adminService.readCollege();
        return ResponseEntity.ok(Map.of(
                "collegeList", collegeList,
                "crud", crud
        ));
	}

	/**
	 * 
	 * @return 단과대학 입력 기능 / 등록 버튼 누르면 실행, crud=insert
	 */
	@PostMapping("/college")
	public ResponseEntity<?> collegeProc(CollegeFormDto collegeFormDto) {
		adminService.createCollege(collegeFormDto);
        return ResponseEntity.ok().body("단과대학 입력이 완료되었습니다");
	}

	/**
	 * 
	 * @return 단과대학 삭제 기능 crud=delete
	 */
	@GetMapping("/collegeDelete")
	public ResponseEntity<?> deleteCollege(@RequestParam Long id) {
		model.addAttribute("id", id);
		adminService.deleteCollege(id);
        return ResponseEntity.ok().body("단과대학 삭제가 완료되었습니다");
	}

	/**
	 * 
	 * @return 학과 페이지 페이지 이동 시, 단과대학 조회 후 이동
	 */
	@GetMapping("/department")
	public ResponseEntity<?> department(@RequestParam(defaultValue = "select") String crud) {
		List<Department> departmentList = adminService.readDepartment(); // 프론트에서 합쳐 찍는 형태일까?
		List<College> collegeList = adminService.readCollege();
//		if (collegeList.isEmpty()) {
//			model.addAttribute("collegeList", null);
//		} else {
//			model.addAttribute("collegeList", collegeList);
//		}
//		if (departmentList.isEmpty()) {
//			model.addAttribute("departmentList", null);
//		} else {
//			model.addAttribute("departmentList", departmentList);
//		}
        return ResponseEntity.ok(Map.of(
                "collegeList", collegeList,
                "departmentList", departmentList,
                "crud", crud
        ));
	}

	/**
	 * 
	 * @return 학과 입력 기능 crud=insert
	 */
	@PostMapping("/department")
	public ResponseEntity<?> departmentProc(DepartmentFormDto departmentFormDto) {
		adminService.createDepartment(departmentFormDto);
        return ResponseEntity.ok().body("학과 입력이 완료되었습니다");
	}

	/**
	 * 
	 * @return 학과 삭제 기능
	 */
	@GetMapping("/departmentDelete")
	public ResponseEntity<?> deleteDepartment(@RequestParam Long id) {
		adminService.deleteDepartment(id);
        return ResponseEntity.ok().body("학과 삭제가 완료되었습니다");
	}

	/**
	 * 
	 * @return 학과 수정 기능
	 */
	@PutMapping("/department")
	public ResponseEntity<?> updateDepartment(DepartmentFormDto departmentFormDto) {
		adminService.updateDepartment(departmentFormDto);
        return ResponseEntity.ok().body("학과 수정이 완료되었습니다");
	}

	/**
	 * 
	 * @return 강의실 페이지
	 */
	@GetMapping("/room")
	public ResponseEntity<?> room(@RequestParam(defaultValue = "select") String crud) {
		List<Room> roomList = adminService.readRoom();
		List<College> collegeList = adminService.readCollege();
//		if (collegeList.isEmpty()) {
//			model.addAttribute("collegeList", null);
//		} else {
//			model.addAttribute("collegeList", collegeList);
//		}
//		if (roomList.isEmpty()) {
//			model.addAttribute("roomList", null);
//		} else {
//			model.addAttribute("roomList", roomList);
//		}
        return ResponseEntity.ok(Map.of(
                "collegeList", collegeList,
                "roomList", roomList,
                "crud", crud
        ));
	}

	/**
	 * 
	 * @return 강의실 입력 기능
	 */
	@PostMapping("/room")
	public ResponseEntity<?> roomProc(RoomFormDto roomFormDto) {
		adminService.createRoom(roomFormDto);
        return ResponseEntity.ok().body("강의실 입력이 완료되었습니다");
	}

	/**
	 * 
	 * @return 강의실 삭제 기능
	 */
	@GetMapping("/roomDelete")
	public ResponseEntity<?> deleteRoom(@RequestParam String id) { // 강의실 기본키(id)는 E601 이런 형식임
		adminService.deleteRoom(id);
        return ResponseEntity.ok().body("강의실 삭제가 완료되었습니다");
	}

	/**
	 * 
	 * @return 강의 페이지
	 */
	@GetMapping("/subject")
    public ResponseEntity<?> subject(@RequestParam(defaultValue = "select") String crud) {
		List<Subject> subjectList = adminService.readSubject();
		List<College> collegeList = adminService.readCollege();
//		if (collegeList.isEmpty()) {
//			model.addAttribute("collegeList", null);
//		} else {
//			model.addAttribute("collegeList", collegeList);
//		}
//		if (subjectList.isEmpty()) {
//			model.addAttribute("subjectList", null);
//		} else {
//			model.addAttribute("subjectList", subjectList);
//		}
        return ResponseEntity.ok(Map.of(
                "collegeList", collegeList,
                "subjectList", subjectList,
                "crud", crud
        ));
	}

	/**
	 * 
	 * @return 강의 입력 기능
	 */
	@PostMapping("/subject")
    public ResponseEntity<?> insertSubject(SubjectFormDto subjectFormDto) {
		adminService.createSubjectAndSyllabus(subjectFormDto);
        return ResponseEntity.ok().body("강의 입력이 완료되었습니다");
	}

	/**
	 * 
	 * @return 강의 삭제 기능
	 */
	@GetMapping("/subjectDelete")
    public ResponseEntity<?> deleteSubject( @RequestParam Long id) {
		adminService.deleteSubject(id);
        return ResponseEntity.ok().body("강의 삭제가 완료되었습니다");
	}

	/**
	 * 
	 * @return 강의 수정 기능
	 */
	@PutMapping("/subject")
    public ResponseEntity<?> updateSubject(SubjectFormDto subjectFormDto) {
		adminService.updateSubject(subjectFormDto);
        return ResponseEntity.ok().body("강의 수정이 완료되었습니다");
	}

	/**
	 * 
	 * @return 단과대별 등록금 페이지
	 */
	@GetMapping("/tuition")
	public ResponseEntity<?> collTuit(@RequestParam(defaultValue = "select") String crud) {
		List<CollTuitFormDto> collTuitList = adminService.readCollTuit();
		List<College> collegeList = adminService.readCollege();
//		if (collegeList.isEmpty()) {
//			model.addAttribute("collegeList", null);
//		} else {
//			model.addAttribute("collegeList", collegeList);
//		}
//		if (collTuitList.isEmpty()) {
//			model.addAttribute("collTuitList", null);
//		} else {
//			model.addAttribute("collTuitList", collTuitList);
//		}
        return ResponseEntity.ok(Map.of(
                "collegeList", collegeList,
                "collTuitList", collTuitList,
                "crud", crud
        ));
	}

	/**
	 * 
	 * @return 단과대별 등록금 입력 기능
	 */
	@PostMapping("/tuition")
	public ResponseEntity<?> insertcollTuit(CollTuitFormDto collTuitFormDto) {
		adminService.createCollTuit(collTuitFormDto);
        return ResponseEntity.ok().body("단과대별 등록금 입력이 완료되었습니다");
	}

	/**
	 * 
	 * @return 단과대 등록금 삭제 기능
	 */
	@GetMapping("/tuitionDelete")
	public ResponseEntity<?> deleteCollTuit(@RequestParam Long collegeId) {
		adminService.deleteCollTuit(collegeId);
        return ResponseEntity.ok().body("단과대별 등록금 삭제가 완료되었습니다");
	}

	/**
	 * 
	 * @return 단과대 등록금 수정 기능
	 */
	@PutMapping("/tuitionUpdate")
	public ResponseEntity<?> updateCollTuit(CollTuitFormDto collTuitFormDto) {
		adminService.updateCollTuit(collTuitFormDto);
        return ResponseEntity.ok().body("단과대별 등록금 수정이 완료되었습니다");
	}

}
