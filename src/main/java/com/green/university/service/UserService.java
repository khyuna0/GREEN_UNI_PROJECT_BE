package com.green.university.service;

import com.green.university.dto.*;
import com.green.university.dto.response.*;
import com.green.university.entity.*;
import com.green.university.handler.exception.CustomRestfullException;
import com.green.university.repository.interfaces.*;
import com.green.university.utils.Define;
import com.green.university.utils.TempPassword;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 유저 서비스
 *
 * @author 김지현
 */
@Service
public class UserService {

	@Autowired
	private StaffRepository staffRepository;
	@Autowired
	private ProfessorRepository professorRepository;
	@Autowired
	private StudentRepository studentRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private StuStatService stuStatService;
	@Autowired
	private StuStatRepository stuStatRepository;
    @Autowired
    private DepartmentRepository departmentRepository;

	/**
	 * staff 생성 서비스로 먼저 staff_tb에 insert한 후 staff_tb에 생긴 id를 끌고와 user_tb에 생성함
	 * 
	 * @param createStaffDto
	 */
    @Transactional
    public void createStaffToStaffAndUser(CreateStaffDto dto) {

        // Staff 저장
        Staff staff = new Staff();
        staff.setName(dto.getName());
        staff.setGender(dto.getGender());
        staff.setAddress(dto.getAddress());
        staff.setTel(dto.getTel());
        staff.setEmail(dto.getEmail());

        Staff savedStaff = staffRepository.save(staff);

        Long staffId = savedStaff.getId(); // 저장한 값으로 유저 테이블 값 생성하기 (로그인 위한 테이블!)

        // User 저장
        User user = new User();
        user.setId(staffId);
        user.setPassword(passwordEncoder.encode(staffId + ""));
        user.setUserRole("staff");

        userRepository.save(user); // INSERT 실행
    }

	/**
	 * professor 생성 서비스 / 먼저 professor_tb에 insert한 후 professor_tb에 생긴 id를 끌고와 user_tb에
	 * 생성함
	 *
	 * @param createStaffDto
	 */
	@Transactional
	public void createProfessorToProfessorAndUser(CreateProfessorDto dto) {

        // Professor 엔티티 생성
        Professor professor = new Professor();
        professor.setName(dto.getName());
        professor.setGender(dto.getGender());
        professor.setEmail(dto.getEmail());
        professor.setTel(dto.getTel());
        professor.setAddress(dto.getAddress());
        professor.setDepartment(departmentRepository.findById(dto.getDeptId()).orElseThrow(() -> new CustomRestfullException("없는 학과 정보입니다.", HttpStatus.NOT_FOUND)));
        // 필요한 필드 모두 dto에서 옮기기

        // 저장 → PK 생성
        Professor savedProfessor = professorRepository.save(professor);
        Long professorId = savedProfessor.getId();

        // User 엔티티 생성
        User user = new User();
        user.setId(professorId);
        user.setPassword(passwordEncoder.encode(professorId + ""));
        user.setUserRole("professor");

        // 저장
        userRepository.save(user);
    }

    /**
     * professor 생성 서비스 먼저 professor_tb에 insert한 후 professor_tb에 생긴 id를 끌고와 user_tb에
     * 생성함
     *
     * @param createStaffDto
     */
    @Transactional
    public void createStudentToStudentAndUser(CreateStudentDto createStudentDto) {
        Department dept = departmentRepository.findById(createStudentDto.getDeptId())
                .orElseThrow(() -> new RuntimeException("학과가 없습니다."));
        Student student = new Student();
        student.setName(createStudentDto.getName());
        student.setBirthDate(createStudentDto.getBirthDate());
        student.setGender(createStudentDto.getGender());
        student.setAddress(createStudentDto.getAddress());
        student.setTel(createStudentDto.getTel());
        student.setDepartment(dept);
        student.setEntranceDate(createStudentDto.getEntranceDate());
        student.setEmail(createStudentDto.getEmail());
        Student savedStudent = studentRepository.save(student);

        Long studentId = savedStudent.getId();
        if (studentId == null) {
            throw new CustomRestfullException(Define.CREATE_FAIL, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        // 학적 상태 생성 (재학)
        stuStatService.createFirstStatus(studentId);

        User user = new User();
        user.setId(studentId);
        user.setPassword(passwordEncoder.encode(studentId + ""));
        user.setUserRole("student");
        userRepository.save(user);

        if (user.getUserRole().equals("student")) {
            throw new CustomRestfullException(Define.CREATE_FAIL, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @Transactional
    public PrincipalDto login(LoginDto loginDto) {
        User user = userRepository.findById(loginDto.getId()).orElseThrow(
                () -> new CustomRestfullException("아이디를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        );
        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            throw new CustomRestfullException(Define.WRONG_PASSWORD, HttpStatus.BAD_REQUEST);
        }
        // 응답 dto로 내보내주기
        PrincipalDto principalDto = new PrincipalDto();
        principalDto.setId(user.getId());
        principalDto.setPassword(user.getPassword());
        principalDto.setUserRole(user.getUserRole());
        // 근데 name은 어디서 내보내줘야함? name 없는 채로 return 하기
        return principalDto;
    }

    /**
     * 학생 수정 대상 정보 불러오기
     *
     * @param userId
     * @return 수정 대상 정보
     */
    public UserInfoForUpdateDto readStudentInfoForUpdate(Long userId) {
        Student student = studentRepository.findById(userId).orElseThrow(
                () -> new CustomRestfullException("학생 정보를 불러올 수 없습니다.", HttpStatus.INTERNAL_SERVER_ERROR)
        );
        UserInfoForUpdateDto userInfoForUpdateDto = new UserInfoForUpdateDto();
        userInfoForUpdateDto.setAddress(student.getAddress());
        userInfoForUpdateDto.setTel(student.getTel());
        userInfoForUpdateDto.setEmail(student.getEmail());
        return userInfoForUpdateDto;
    }

    /**
     * 직원 수정 대상 정보 불러오기
     *
     * @param userId
     * @return 수정 대상 정보
     */
    public UserInfoForUpdateDto readStaffInfoForUpdate(Long userId) {

        Staff staff = staffRepository.findById(userId).orElseThrow(() -> new CustomRestfullException("없는 직원 정보입니다.", HttpStatus.NOT_FOUND));

		UserInfoForUpdateDto userInfoForUpdateDto = new UserInfoForUpdateDto();
        userInfoForUpdateDto.setAddress(staff.getAddress());
        userInfoForUpdateDto.setTel(staff.getTel());
        userInfoForUpdateDto.setEmail(staff.getEmail());

        return userInfoForUpdateDto;
    }

    /**
     * 교수 수정 대상 정보 불러오기
     *
     * @param userId
     * @return 수정 대상 정보
     */
    public UserInfoForUpdateDto readProfessorInfoForUpdate(Long userId) {

        Professor professor = professorRepository.findById(userId).orElseThrow(() -> new CustomRestfullException("없는 교수 정보입니다.", HttpStatus.NOT_FOUND));

        UserInfoForUpdateDto userInfoForUpdateDto = new UserInfoForUpdateDto();
        userInfoForUpdateDto.setAddress(professor.getAddress());
        userInfoForUpdateDto.setTel(professor.getTel());
        userInfoForUpdateDto.setEmail(professor.getEmail());

        return userInfoForUpdateDto;
    }

    /**
     * 학생 정보 수정
     *
     * @param updateDto
     */
    @Transactional
    public void updateStudent(UserUpdateDto updateDto) {
        // User 테이블의 userid 찾아서 -> 그 userid로 Student 객체 찾기
        Long userId = updateDto.getUserId();
        User user = userRepository.findById(userId).orElseThrow(
                () -> new CustomRestfullException(Define.NOT_FOUND_ID, HttpStatus.NOT_FOUND)
        );
        Student student = studentRepository.findById(user.getId()).orElseThrow(
                () -> new CustomRestfullException(Define.NOT_FOUND_ID, HttpStatus.NOT_FOUND)
        );
        student.setAddress(updateDto.getAddress());
        student.setTel(updateDto.getTel());
        student.setEmail(updateDto.getEmail());
        Student updatedStudent = studentRepository.save(student);
        studentRepository.save(updatedStudent);
    }

	/**
	 * 직원 정보 수정
	 * 
	 * @param updateDto
	 */
	@Transactional
	public void updateStaff(UserUpdateDto dto) {

        Professor professor = professorRepository.findById(dto.getUserId()).orElseThrow(() -> new CustomRestfullException("없는 직원 정보입니다.", HttpStatus.NOT_FOUND));;

        professor.setAddress(dto.getAddress());
        professor.setTel(dto.getTel());
        professor.setEmail(dto.getEmail());

        professorRepository.save(professor);
	}

    /**
     * 교수 정보 수정
     *
     * @param updateDto
     */
    @Transactional
    public void updateProfessor(UserUpdateDto updateDto) {

        Professor professor = professorRepository.findById(updateDto.getUserId()).orElseThrow(() -> new CustomRestfullException("없는 직원 정보입니다.", HttpStatus.NOT_FOUND));;

        professor.setAddress(updateDto.getAddress());
        professor.setTel(updateDto.getTel());
        professor.setEmail(updateDto.getEmail());

        professorRepository.save(professor);

    }

    /**
     * 비밀번호 변경
     *
     * @param changePasswordDto
     */
    @Transactional
    public void updatePassword(ChangePasswordDto changePasswordDto) {
        User user = userRepository.findById(changePasswordDto.getId()).orElseThrow(
                () -> new CustomRestfullException("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        );
        user.setPassword(changePasswordDto.getAfterPassword());
        userRepository.save(user);
    }

    /**
     * 학생 조회
     *
     * @param studentId
     * @return studentEntity
     */
    @Transactional
    public Student readStudent(Long studentId) {
        return studentRepository.findById(studentId).orElseThrow(
                () -> new CustomRestfullException("학생을 조회할 수 없습니다.", HttpStatus.NOT_FOUND)
        );
    }

	/**
	 * 직원 조회
	 * 
	 * @param id
	 * @return staffEntity
	 */
	@Transactional
	public Staff readStaff(Long id) {
		Staff staffEntity = staffRepository.findById(id).orElseThrow(() -> new CustomRestfullException("없는 직원 정보입니다.", HttpStatus.NOT_FOUND));
		return staffEntity;
	}

	/**
	 * 교수 정보 조회
	 * 
	 * @param id
	 * @return professorEntity
	 */
	@Transactional
	public ProfessorInfoDto readProfessorInfo(Long id) {

        Professor professor = professorRepository.findById(id).orElseThrow(() -> new CustomRestfullException("없는 교수 정보입니다.", HttpStatus.NOT_FOUND));


        ProfessorInfoDto dto = new ProfessorInfoDto();
        dto.setId(professor.getId());
        dto.setName(professor.getName());
        dto.setBirthDate(professor.getBirthDate());
        dto.setGender(professor.getGender());
        dto.setAddress(professor.getAddress());
        dto.setTel(professor.getTel());
        dto.setEmail(professor.getEmail());
        dto.setHireDate(professor.getHireDate());

        // 마이페이지에서 소속 : 공과대학 컴퓨터공학과 ( CollegeName DeptName ) 형식으로 출력됨 그래서 Dto 사용하나봄

        dto.setCollegeName(professor.getDepartment().getCollege().getName());
        dto.setDeptName(professor.getDepartment().getName());

		return dto;
	}

    /**
     * 학생 정보 조회
     *
     * @param id
     * @return StudentEntity
     */
    @Transactional
    public StudentInfoDto readStudentInfo(Long id) {
        // id로 student 엔티티 찾아서 dto로 변환해서 반환
        Student student = studentRepository.findById(id).orElseThrow(
                () -> new CustomRestfullException("학생 정보를 찾을 수 없습니다", HttpStatus.INTERNAL_SERVER_ERROR)
        );
        return StudentInfoDto.fromEntity(student);
    }

    /**
     * 아이디 찾기
     *
     * @param findIdFormDto
     * @return
     */
    @Transactional
    public Long readIdByNameAndEmail(FindIdFormDto findIdFormDto) {
        Long findId = null;
        if (findIdFormDto.getUserRole().equals("student")) {
            findId = studentRepository.findByNameAndEmail(findIdFormDto.getName(), findIdFormDto.getEmail());
        } else if (findIdFormDto.getUserRole().equals("professor")) {
            findId = professorRepository.findByNameAndEmail(findIdFormDto.getName(), findIdFormDto.getEmail()).getId();
        } else if (findIdFormDto.getUserRole().equals("staff")) {
            findId = staffRepository.findByNameAndEmail(findIdFormDto.getName(), findIdFormDto.getEmail()).getId();
        }

        if (findId == null) {
            throw new CustomRestfullException("아이디를 찾을 수 없습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return findId;

    }

    /**
     * 비밀번호 찾기
     *
     * @param findPasswordFormDto
     * @return
     */
    @Transactional
    public String updateTempPassword(FindPasswordFormDto findPasswordFormDto) {
        // dto에 userrole과 user의 userrole을 어떻게 잘 맞춰서 쓸 수 있을까..
        Long userId = findPasswordFormDto.getId();
        String userName = findPasswordFormDto.getName();
        String userEmail = findPasswordFormDto.getEmail();

        Long findId = 0L;

        // 추후에 역할별 ID 조회용 함수 맵을 생성할 수도 있다
        if (findPasswordFormDto.getUserRole().equals("student")) {
            findId = studentRepository.findByIdAndNameAndEmail(userId, userName, userEmail);
            if (findId == null) {
                throw new CustomRestfullException("조건에 맞는 정보를 찾을 수 없습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else if (findPasswordFormDto.getUserRole().equals("professor")) {
            findId = professorRepository.findByIdAndNameAndEmail(findPasswordFormDto.getId(),findPasswordFormDto.getName(),findPasswordFormDto.getEmail()).getId();
            if (findId == null) {
                throw new CustomRestfullException("조건에 맞는 정보를 찾을 수 없습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else if (findPasswordFormDto.getUserRole().equals("staff")) {
            findId = staffRepository.findByIdAndNameAndEmail(findPasswordFormDto.getId(),findPasswordFormDto.getName(),findPasswordFormDto.getEmail()).getId();
            if (findId == null) {
                throw new CustomRestfullException("조건에 맞는 정보를 찾을 수 없습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        String tempPassword = new TempPassword().returnTempPassword(); // 임시 비밀번호 생성
        System.out.println(tempPassword);

        User user = userRepository.findById(userId).orElseThrow(
                () -> new CustomRestfullException("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        );
        user.setPassword(passwordEncoder.encode(tempPassword));
        userRepository.save(user);

        return tempPassword;

    }

    // studentId로 학생의 학적 변동 내역(StuStat)을 StudentInfoStatListDto로 가져오기
    public List<StudentInfoStatListDto> readStudentInfoStatListByStudentId(Long studentId) {
        //sta를 찾아 2개 나옴 -> 이걸 모두 찍어줄거임
        List<StuStat> stuStatList = stuStatRepository.findByStudent_IdOrderByIdDesc(studentId);

        // 찍어줘야하는 dto
        List<StudentInfoStatListDto> result = new ArrayList<>();

        for (StuStat stuStat : stuStatList) {
            StudentInfoStatListDto dto = new StudentInfoStatListDto();
            dto.setFromDate(stuStat.getFromDate());
            dto.setStatus(stuStat.getStatus());

            // 휴학 신청 여부 확인
            BreakApp breakApp = stuStat.getBreakApp();
            if (breakApp != null) {
                dto.setDetail(breakApp.getType());
                dto.setAdopt(breakApp.getStatus());
                dto.setToYear(breakApp.getToYear());
                dto.setToSemester(breakApp.getToSemester());
            } else {
                dto.setDetail(null);
                dto.setAdopt(null);
                dto.setToYear(null);
                dto.setToSemester(null);
            }

            result.add(dto);
        }

        return result;

        /* 간단버전
         return stuStatList.stream()
            .map(stuStat -> {
                StudentInfoStatListDto dto = new StudentInfoStatListDto();
                dto.setFromDate(stuStat.getFromDate());
                dto.setStatus(stuStat.getStatus());

                BreakApp breakApp = stuStat.getBreakApp();
                if (breakApp != null) {
                    dto.setDetail(breakApp.getType());
                    dto.setAdopt(breakApp.getStatus());
                    dto.setToYear(breakApp.getToYear());
                    dto.setToSemester(breakApp.getToSemester());
                }
                return dto;
            })
            .collect(Collectors.toList());
        */
    }

}
