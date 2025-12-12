package com.green.university.controller;

import com.green.university.dto.*;
import com.green.university.entity.College;
import com.green.university.entity.Department;
import com.green.university.entity.Room;
import com.green.university.entity.Subject;
import com.green.university.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
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
@RequestMapping("/api/admin")
public class AdminController {
    @Autowired
    private AdminService adminService;


    /**
     *
     * 단과대
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

    // 단과대 등록
    @PostMapping("/college")
    public ResponseEntity<?> collegeProc(@RequestBody@Valid  CollegeFormDto collegeFormDto) {
        adminService.createCollege(collegeFormDto);
        return ResponseEntity.ok().body("단과대학 입력이 완료되었습니다.");
    }

    // 단과대 삭제
    @DeleteMapping("/college/{collegeId}")
    public ResponseEntity<?> deleteCollege(@PathVariable("collegeId") Long collegeId) {
        adminService.deleteCollege(collegeId);
        return ResponseEntity.ok().body("단과대학 삭제가 완료되었습니다.");
    }

    // 단과대 수정
    @PatchMapping("/college/{collegeId}")
    public ResponseEntity<?> updateCollege(
            @PathVariable("collegeId") Long collegeId,
            @RequestBody @Valid CollegeFormDto collegeFormDto) {

        adminService.updateCollege(collegeId, collegeFormDto);
        return ResponseEntity.ok().body("단과대학 수정이 완료되었습니다.");
    }


    /**
     *
     *  학과
     */

    @GetMapping("/department")
    public ResponseEntity<?> department(@RequestParam(defaultValue = "select") String crud) {
        List<Department> departmentList = adminService.readDepartment(); // 프론트에서 합쳐 찍는 형태일까?
        List<College> collegeList = adminService.readCollege();
        return ResponseEntity.ok(Map.of(
                "collegeList", collegeList,
                "departmentList", departmentList,
                "crud", crud
        ));
    }

    // 학과 입력 기능 crud=insert
    @PostMapping("/department")
    public ResponseEntity<?> departmentProc(@RequestBody @Valid DepartmentFormDto departmentFormDto) {
        adminService.createDepartment(departmentFormDto);
        return ResponseEntity.ok().body("학과 입력이 완료되었습니다");
    }

    // 학과 삭제 기능
    @DeleteMapping("/department/{deptId}")
    public ResponseEntity<?> deleteDepartment(@PathVariable("deptId") Long deptId) {
        adminService.deleteDepartment(deptId);
        return ResponseEntity.ok().body("학과 삭제가 완료되었습니다");
    }

    // 학과 수정 기능
    @PatchMapping("/department/{deptId}")
    public ResponseEntity<?> updateDepartment(@PathVariable("deptId") Long deptId, @RequestBody @Valid DepartmentFormDto departmentFormDto) {
        adminService.updateDepartment(deptId, departmentFormDto);
        return ResponseEntity.ok().body("학과 수정이 완료되었습니다");
    }


    /**
     *
     *   강의실
     */

    @GetMapping("/room")
    public ResponseEntity<?> room(@RequestParam(defaultValue = "select") String crud) {
        List<Room> roomList = adminService.readRoom();
        List<College> collegeList = adminService.readCollege();

        return ResponseEntity.ok(Map.of(
                "collegeList", collegeList,
                "roomList", roomList,
                "crud", crud
        ));
    }

    // 강의실 입력 기능
    @PostMapping("/room")
    public ResponseEntity<?> roomProc(@Valid @RequestBody RoomFormDto roomFormDto) {
        adminService.createRoom(roomFormDto);
        return ResponseEntity.ok().body("강의실 입력이 완료되었습니다");
    }

    // 강의실 삭제 기능
    @DeleteMapping("/room/{roomId}")
    public ResponseEntity<?> deleteRoom(@PathVariable("roomId") String roomId) { // 강의실 기본키(id)는 E601 이런 형식임
        adminService.deleteRoom(roomId);
        return ResponseEntity.ok().body("강의실 삭제가 완료되었습니다");
    }

    // 강의실 수정 기능
    @PatchMapping("/room/{roomId}")
    public ResponseEntity<?> updateRoom(@PathVariable("roomId") String roomId,@RequestBody@Valid RoomFormDto roomFormDto){
        adminService.updateRoom(roomId, roomFormDto);
        return ResponseEntity.ok().body("강의실이 수정 되었습니다.");
    }


    /**
     *
     *   강의
     */

    // 강의 페이지 - 리스트 조회
    @GetMapping("/subject")
    public ResponseEntity<?> subject(@RequestParam(defaultValue = "select") String crud) {
        List<Subject> subjectList = adminService.readSubject();
        List<College> collegeList = adminService.readCollege();

        return ResponseEntity.ok(Map.of(
                "collegeList", collegeList,
                "subjectList", subjectList,
                "crud", crud
        ));
    }

    // 강의 등록
    @PostMapping("/subject")
    public ResponseEntity<?> insertSubject(@Valid @RequestBody SubjectFormDto subjectFormDto) {
        adminService.createSubjectAndSyllabus(subjectFormDto);
        return ResponseEntity.ok().body("강의 입력이 완료되었습니다");
    }

    // 강의 삭제
    @DeleteMapping("/subject/{subjectId}")
    public ResponseEntity<?> deleteSubject(@PathVariable("subjectId") Long subjectId) {
        adminService.deleteSubject(subjectId);
        return ResponseEntity.ok().body("강의 삭제가 완료되었습니다");
    }

    // 강의 수정
    @PatchMapping("/subject/{subjectId}")
    public ResponseEntity<?> updateSubject(@PathVariable("subjectId") Long subjectId, @RequestBody @Valid SubjectFormDto subjectFormDto) {
        adminService.updateSubject(subjectId, subjectFormDto);
        return ResponseEntity.ok().body("강의 수정이 완료되었습니다");
    }

    /**
     *
     * 단과대 등록금
     */

    // 단과대 등록금 페이지
    @GetMapping("/tuition")
    public ResponseEntity<?> collTuit(@RequestParam(defaultValue = "select") String crud) {
        List<CollTuitFormDto> collTuitList = adminService.readCollTuit();
        List<College> collegeList = adminService.readCollege();

        return ResponseEntity.ok(Map.of(
                "collegeList", collegeList,
                "collTuitList", collTuitList,
                "crud", crud
        ));
    }

    // 단과대 등록금 입력
    @PostMapping("/tuition")
    public ResponseEntity<?> insertcollTuit(@RequestBody @Valid CollTuitFormDto collTuitFormDto) {
        adminService.createCollTuit(collTuitFormDto);
        return ResponseEntity.ok().body("단과대별 등록금 입력이 완료되었습니다");
    }

    // 단과대 등록금 삭제
    @DeleteMapping("/tuition/{collegeId}")
    public ResponseEntity<?> deleteCollTuit(@PathVariable("collegeId") Long collegeId) {
        adminService.deleteCollTuit(collegeId);
        return ResponseEntity.ok().body("단과대별 등록금 삭제가 완료되었습니다");
    }

    // 단과대 등록금 수정
    @PatchMapping("/tuition/{collegeId}")
    public ResponseEntity<?> updateCollTuit(@PathVariable("collegeId") Long collegeId ,  @RequestBody @Valid CollTuitFormDto collTuitFormDto) {
        adminService.updateCollTuit(collegeId, collTuitFormDto);
        return ResponseEntity.ok().body("단과대별 등록금 수정이 완료되었습니다");
    }

}
