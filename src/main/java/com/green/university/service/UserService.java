package com.green.university.service;

import com.green.university.dto.*;
import com.green.university.dto.response.*;
import com.green.university.handler.exception.CustomRestfullException;
import com.green.university.repository.interfaces.*;
import com.green.university.entity.Staff;
import com.green.university.entity.Student;
import com.green.university.entity.User;
import com.green.university.utils.Define;
import com.green.university.utils.TempPassword;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
	 * professor 생성 서비스 먼저 professor_tb에 insert한 후 professor_tb에 생긴 id를 끌고와 user_tb에
	 * 생성함
	 * 
	 * @param createStaffDto
	 */
	@Transactional
	public void createProfessorToProfessorAndUser(CreateProfessorDto createProfessorDto) {

		Long resultCountRow = professorRepository.insertToProfessor(createProfessorDto);

		if (resultCountRow != 1) {
			throw new CustomRestfullException(Define.CREATE_FAIL, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		Long professorId = professorRepository.selectIdByCreateProfessorDto(createProfessorDto);

		User user = new User();
		user.setId(professorId);
		user.setPassword(passwordEncoder.encode(professorId + ""));
		user.setUserRole("professor");

		resultCountRow = userRepository.insertToUser(user);
		if (resultCountRow != 1) {
			throw new CustomRestfullException(Define.CREATE_FAIL, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	/**
	 * professor 생성 서비스 먼저 professor_tb에 insert한 후 professor_tb에 생긴 id를 끌고와 user_tb에
	 * 생성함
	 * 
	 * @param createStaffDto
	 */
	@Transactional
	public void createStudentToStudentAndUser(CreateStudentDto createStudentDto) {
		Long resultCountRow = studentRepository.insertToStudent(createStudentDto);

		if (resultCountRow != 1) {
			throw new CustomRestfullException(Define.CREATE_FAIL, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		Long studentId = studentRepository.selectIdByCreateStudentDto(createStudentDto);

		// 학적 상태 생성 (재학)
		stuStatService.createFirstStatus(studentId);

		User user = new User();
		user.setId(studentId);
		user.setPassword(passwordEncoder.encode(studentId + ""));
		user.setUserRole("student");

		resultCountRow = userRepository.insertToUser(user);
		if (resultCountRow != 1) {
			throw new CustomRestfullException(Define.CREATE_FAIL, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@Transactional
	public PrincipalDto login(LoginDto loginDto) {
		PrincipalDto userEntity = userRepository.selectById(loginDto.getId());

		if (userEntity == null) {
			System.out.println("564156456");
			throw new CustomRestfullException(Define.NOT_FOUND_ID, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		if (!passwordEncoder.matches(loginDto.getPassword(), userEntity.getPassword())) {
			throw new CustomRestfullException(Define.WRONG_PASSWORD, HttpStatus.BAD_REQUEST);
		}

		return userEntity;
	}

	/**
	 * 학생 수정 대상 정보 불러오기
	 * 
	 * @param userId
	 * @return 수정 대상 정보
	 */
	public UserInfoForUpdateDto readStudentInfoForUpdate(Long userId) {

		UserInfoForUpdateDto userInfoForUpdateDto = studentRepository.selectByUserId(userId);

		return userInfoForUpdateDto;
	}

	/**
	 * 직원 수정 대상 정보 불러오기
	 * 
	 * @param userId
	 * @return 수정 대상 정보
	 */
	public UserInfoForUpdateDto readStaffInfoForUpdate(Long userId) {

		UserInfoForUpdateDto userInfoForUpdateDto = staffRepository.selectByUserId(userId);

		return userInfoForUpdateDto;
	}

	/**
	 * 교수 수정 대상 정보 불러오기
	 * 
	 * @param userId
	 * @return 수정 대상 정보
	 */
	public UserInfoForUpdateDto readProfessorInfoForUpdate(Long userId) {

		UserInfoForUpdateDto userInfoForUpdateDto = professorRepository.selectByUserId(userId);

		return userInfoForUpdateDto;
	}

	/**
	 * 학생 정보 수정
	 * 
	 * @param updateDto
	 */
	@Transactional
	public void updateStudent(UserUpdateDto updateDto) {

		Long resultCountRaw = studentRepository.updateStudent(updateDto);
		if (resultCountRaw != 1) {
			throw new CustomRestfullException(Define.UPDATE_FAIL, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	/**
	 * 직원 정보 수정
	 * 
	 * @param updateDto
	 */
	@Transactional
	public void updateStaff(UserUpdateDto updateDto) {

		Long resultCountRaw = staffRepository.updateStaff(updateDto);
		if (resultCountRaw != 1) {
			throw new CustomRestfullException(Define.UPDATE_FAIL, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	/**
	 * 교수 정보 수정
	 * 
	 * @param updateDto
	 */
	@Transactional
	public void updateProfessor(UserUpdateDto updateDto) {

		Long resultCountRaw = professorRepository.updateProfessor(updateDto);
		if (resultCountRaw != 1) {
			throw new CustomRestfullException(Define.UPDATE_FAIL, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	/**
	 * 비밀번호 변경
	 * 
	 * @param changePasswordDto
	 */
	@Transactional
	public void updatePassword(ChangePasswordDto changePasswordDto) {
		Long resultCountRaw = userRepository.updatePassword(changePasswordDto);
		if (resultCountRaw != 1) {
			throw new CustomRestfullException(Define.UPDATE_FAIL, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * 학생 조회
	 * 
	 * @param studentId
	 * @return studentEntity
	 */
	@Transactional
	public Student readStudent(Long studentId) {
		Student studentEntity = studentRepository.selectByStudentId(studentId);
		return studentEntity;
	}

	/**
	 * 직원 조회
	 * 
	 * @param id
	 * @return staffEntity
	 */
	@Transactional
	public Staff readStaff(Long id) {
		Staff staffEntity = staffRepository.selectStaffById(id);
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
		ProfessorInfoDto professorEntity = professorRepository.selectProfessorInfoById(id);
		return professorEntity;
	}

	/**
	 * 학생 정보 조회
	 * 
	 * @param id
	 * @return StudentEntity
	 */
	@Transactional
	public StudentInfoDto readStudentInfo(Long id) {
		StudentInfoDto studentEntity = studentRepository.selectStudentInfoById(id);
		return studentEntity;
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
			findId = studentRepository.selectIdByNameAndEmail(findIdFormDto);
		} else if (findIdFormDto.getUserRole().equals("professor")) {
			findId = professorRepository.selectIdByNameAndEmail(findIdFormDto);
		} else if (findIdFormDto.getUserRole().equals("staff")) {
			findId = staffRepository.selectIdByNameAndEmail(findIdFormDto);
		}

		if (findId == null) {
			throw new CustomRestfullException("아이디를 찾을 수 없습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return findId;

	}

	/**
	 * 아이디 찾기
	 * 
	 * @param findIdFormDto
	 * @return
	 */
	@Transactional
	public String updateTempPassword(FindPasswordFormDto findPasswordFormDto) {

		String password = null;

		Long findId = 0L;

		if (findPasswordFormDto.getUserRole().equals("student")) {
			findId = studentRepository.selectStudentByIdAndNameAndEmail(findPasswordFormDto);
			if (findId == null) {
				throw new CustomRestfullException("조건에 맞는 정보를 찾을 수 없습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else if (findPasswordFormDto.getUserRole().equals("professor")) {
			findId = professorRepository.selectProfessorByIdAndNameAndEmail(findPasswordFormDto);
			if (findId == null) {
				throw new CustomRestfullException("조건에 맞는 정보를 찾을 수 없습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else if (findPasswordFormDto.getUserRole().equals("staff")) {
			findId = staffRepository.selectStaffByIdAndNameAndEmail(findPasswordFormDto);
			if (findId == null) {
				throw new CustomRestfullException("조건에 맞는 정보를 찾을 수 없습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}

		password = new TempPassword().returnTempPassword();
		System.out.println(password);
		ChangePasswordDto changePasswordDto = new ChangePasswordDto();
		changePasswordDto.setAfterPassword(passwordEncoder.encode(password));
		changePasswordDto.setId(findPasswordFormDto.getId());
		userRepository.updatePassword(changePasswordDto);

		return password;

	}

	public List<StudentInfoStatListDto> readStudentInfoStatListByStudentId(Long studentId) {

		List<StudentInfoStatListDto> list = stuStatRepository.selectStuStatListBystudentId(studentId);

		return list;
	}

}
