package com.green.university.domain.admin.controller;

import com.green.university.domain.admin.dto.*;
import com.green.university.domain.admin.entity.Staff;
import com.green.university.domain.admin.service.UserService;
import com.green.university.domain.breakapp.entity.BreakApp;
import com.green.university.domain.breakapp.service.BreakAppService;
import com.green.university.domain.notice.dto.NoticeFormDto;
import com.green.university.domain.notice.service.NoticeService;
import com.green.university.domain.professor.dto.ProfessorInfoDto;
import com.green.university.domain.schedule.entity.Schedule;
import com.green.university.domain.schedule.service.ScheduleService;
import com.green.university.domain.student.dto.StuStatDto;
import com.green.university.domain.student.dto.StudentInfoDto;
import com.green.university.domain.student.dto.StudentInfoStatListDto;
import com.green.university.domain.student.service.StuStatService;
import com.green.university.global.exception.CustomRestfullException;
import com.green.university.global.security.CustomUserDetails;
import com.green.university.global.security.JwtUtil;
import com.green.university.global.utils.Define;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/personal")
public class PersonalController {

    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private StuStatService stuStatService;
    @Autowired
    private BreakAppService breakAppService;
    @Autowired
    private NoticeService noticeService;
    @Autowired
    private ScheduleService scheuleService;
    @Autowired
    private JwtUtil jwtUtil;

    // 메인 홈에 필요한 데이터 (공지, 일정, 사용자 정보)
    @GetMapping
    @PreAuthorize("hasAnyRole('STUDENT', 'PROFESSOR', 'STAFF')")
    public ResponseEntity<?> home(@AuthenticationPrincipal CustomUserDetails principal) {

        Long userId = principal.getId();
        String userRole = principal.getUserRole();

        // 공지사항 최신 글 5개
        List<NoticeFormDto> noticeList = noticeService.readCurrentNotice();

        // 학사일정 (기존 로직 : 2023년 2월 고정)
        List<Schedule> scheduleList = scheuleService.readScheduleListByMonth(2023, 2);

        Object userInfo = null;
        String currentStatus = null;
        Integer breakAppSize = null;

        if ("student".equals(userRole)) {
            StudentInfoDto studentInfo = userService.readStudentInfo(userId);
            StuStatDto stuStat = stuStatService.readCurrentStatus(userId);
            userInfo = studentInfo;
            currentStatus = stuStat.getStatus();

        } else if ("staff".equals(userRole)) {
            Staff staffInfo = userService.readStaff(userId);
            userInfo = staffInfo;

            List<BreakApp> breakAppList = breakAppService.readByStatus("처리중");
            breakAppSize = breakAppList.size();

        } else if ("professor".equals(userRole)) {
            ProfessorInfoDto professorInfo = userService.readProfessorInfo(userId);
            userInfo = professorInfo;
        }

        return ResponseEntity.ok(Map.of(
                "userId", userId,
                "userRole", userRole,
                "noticeList", noticeList,
                "scheduleList", scheduleList,
                "userInfo", userInfo,
                "currentStatus", currentStatus,
                "breakAppSize", breakAppSize
        ));
    }

    // 내 정보 수정 페이지용 데이터
    @GetMapping("/update")
    public ResponseEntity<?> getUserInfoForUpdate(@AuthenticationPrincipal CustomUserDetails principal) {

        if (principal == null) {
            throw new CustomRestfullException("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);
        }

        Long userId = principal.getId();
        String role = principal.getUserRole();

        UserInfoForUpdateDto userInfoForUpdateDto;

        if ("staff".equals(role)) {
            userInfoForUpdateDto = userService.readStaffInfoForUpdate(userId);
        } else if ("student".equals(role)) {
            userInfoForUpdateDto = userService.readStudentInfoForUpdate(userId);
        } else if ("professor".equals(role)) {
            userInfoForUpdateDto = userService.readProfessorInfoForUpdate(userId);
        } else {
            throw new CustomRestfullException("알 수 없는 사용자 권한입니다.", HttpStatus.BAD_REQUEST);
        }

        return ResponseEntity.ok(Map.of(
                "userRole", role,
                "userInfo", userInfoForUpdateDto
        ));
    }

    // 3) 내 정보 수정 처리
    @PatchMapping("/update")
    public ResponseEntity<?> updateUserProc(
            @Valid @RequestBody UserInfoForUpdateDto userInfoForUpdateDto,
            @RequestParam String password,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {

        if (principal == null) {
            throw new CustomRestfullException("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);
        }

        Long userId = principal.getId();
        String role = principal.getUserRole();

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(password, principal.getPassword())) {
            throw new CustomRestfullException(Define.WRONG_PASSWORD, HttpStatus.BAD_REQUEST);
        }

        UserUpdateFormDto updateDto = new UserUpdateFormDto();
        updateDto.setUserId(userId);
        updateDto.setAddress(userInfoForUpdateDto.getAddress());
        updateDto.setEmail(userInfoForUpdateDto.getEmail());
        updateDto.setTel(userInfoForUpdateDto.getTel());

        if ("staff".equals(role)) {
            userService.updateStaff(updateDto);
        } else if ("student".equals(role)) {
            userService.updateStudent(updateDto);
        } else if ("professor".equals(role)) {
            userService.updateProfessor(updateDto);
        } else {
            throw new CustomRestfullException("알 수 없는 사용자 권한입니다.", HttpStatus.BAD_REQUEST);
        }


        return ResponseEntity.ok(Map.of(
                "message", "개인정보가 수정되었습니다."

        ));
    }

    // 비밀번호 변경 페이지
    @GetMapping("/password")
    public ResponseEntity<?> getPasswordPage(@AuthenticationPrincipal CustomUserDetails principal) {
        if (principal == null) {
            throw new CustomRestfullException("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);
        }
        return ResponseEntity.ok(Map.of(
                "userId", principal.getId(),
                "userRole", principal.getUserRole()
        ));
    }

    // 5) 비밀번호 변경 처리
    @PatchMapping("/password")
    public ResponseEntity<?> updatePasswordProc(
            @Valid @RequestBody ChangePasswordFormDto changePasswordFormDto,
            BindingResult bindingResult,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {

        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            bindingResult.getAllErrors().forEach(error -> {
                sb.append(error.getDefaultMessage()).append("\\n");
            });
            throw new CustomRestfullException(sb.toString(), HttpStatus.BAD_REQUEST);
        }

        if (principal == null) {
            throw new CustomRestfullException("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);
        }

        Long userId = principal.getId();

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(changePasswordFormDto.getBeforePassword(), principal.getPassword())) {
            throw new CustomRestfullException(Define.WRONG_PASSWORD, HttpStatus.BAD_REQUEST);
        }
        // 새 비밀번호 & 확인 일치 확인
        if (!changePasswordFormDto.getAfterPassword().equals(changePasswordFormDto.getPasswordCheck())) {
            throw new CustomRestfullException("변경할 비밀번호와 비밀번호 확인은 같아야합니다.", HttpStatus.BAD_REQUEST);
        }

        changePasswordFormDto.setId(userId);
        changePasswordFormDto.setAfterPassword(passwordEncoder.encode(changePasswordFormDto.getAfterPassword()));
        userService.updatePassword(changePasswordFormDto);

        return ResponseEntity.ok(Map.of(
                "message", "비밀번호가 변경되었습니다."
        ));
    }

    // 학생 정보 조회
    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/info/student")
    public ResponseEntity<?> readStudentInfo(@AuthenticationPrincipal CustomUserDetails principal) {

        if (principal == null) {
            throw new CustomRestfullException("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);
        }

        Long userId = principal.getId();

        StudentInfoDto student = userService.readStudentInfo(userId);
        List<StudentInfoStatListDto> list = userService.readStudentInfoStatListByStudentId(userId);

        return ResponseEntity.ok(Map.of(
                "student", student,
                "stuStat", list
        ));
    }

    // 직원 정보 조회
    @GetMapping("/info/staff")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<?> readStaffInfo(@AuthenticationPrincipal CustomUserDetails principal) {

        if (principal == null) {
            throw new CustomRestfullException("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);
        }

        Long userId = principal.getId();
        Staff staff = userService.readStaff(userId);

        return ResponseEntity.ok(Map.of(
                "staff", staff
        ));
    }

    // 교수 정보 조회
    @GetMapping("/info/professor")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<?> readProfessorInfo(@AuthenticationPrincipal CustomUserDetails principal) {

        if (principal == null) {
            throw new CustomRestfullException("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);
        }

        Long userId = principal.getId();
        ProfessorInfoDto professor = userService.readProfessorInfo(userId);

        return ResponseEntity.ok(Map.of(
                "professor", professor
        ));
    }

    // 아이디 찾기
    @PostMapping("/find/id")
    public ResponseEntity<?> findIdProc(
            @Valid @RequestBody FindIdFormDto findIdFormDto
    ) {
        Long findId = userService.readIdByNameAndEmail(findIdFormDto);

        return ResponseEntity.ok(Map.of(
                "id", findId,
                "name", findIdFormDto.getName()
        ));
    }

    // 비밀번호 찾기
    @PostMapping("/find/password")
    public ResponseEntity<?> findPasswordProc(
            @Valid @RequestBody FindPasswordFormDto findPasswordFormDto
    ) {
        String password = userService.updateTempPassword(findPasswordFormDto);

        return ResponseEntity.ok(Map.of(
                "name", findPasswordFormDto.getName(),
                "password", password
        ));
    }

}