package com.green.university.service;

import com.green.university.dto.*;
import com.green.university.entity.*;
import com.green.university.exception.CustomRestfullException;
import com.green.university.repository.interfaces.*;
import com.green.university.utils.SubjectUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 *
 * @author 박성희
 *
 */

@Service
public class AdminService {
    @Autowired
    private CollegeRepository collegeRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private CollTuitRepository collTuitRepository;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private SubjectRepository subjectRepository;
    @Autowired
    private SyllaBusRepository syllaBusRepository;
    @Autowired
    private ProfessorRepository professorRepository;

    // 단과대 입력
    @Transactional
    public void createCollege(@Validated CollegeFormDto collegeFormDto) {
        // 같은 이름 중복 검사
        if(collegeRepository.existsByName(collegeFormDto.getName())){
            throw new CustomRestfullException("이미 존재하는 단과대입니다", HttpStatus.INTERNAL_SERVER_ERROR, "/break/appList");
        }

        College college = new College();
        college.setName(collegeFormDto.getName());
        collegeRepository.save(college);
    }

    // 단과대 조회
    @Transactional
    public List<College> readCollege() {
        return collegeRepository.findAll();
    }

    // 단과대 삭제
    public void deleteCollege(Long collegeId) {
        collegeRepository.deleteById(collegeId);
    }

    /**
     * 학과 입력 서비스
     */
    @Transactional
    public void createDepartment(@Validated DepartmentFormDto departmentFormDto) {

        // 같은 학과 이름이 이미 존재하는지 검사
        if (departmentRepository.existsByName(departmentFormDto.getName())) {
            throw new CustomRestfullException("이미 존재하는 학과입니다", HttpStatus.INTERNAL_SERVER_ERROR, "/break/appList");
        }

        Department department = new Department();
        department.setName(departmentFormDto.getName());

        //collegeId 까지 매핑?
        College college = collegeRepository.findById(departmentFormDto.getCollegeId())
                .orElseThrow(() -> new CustomRestfullException("단과대를 찾을 수 없습니다.", HttpStatus.NOT_FOUND, "/break/appList"));
        department.setCollege(college);

        departmentRepository.save(department);

    }

    // 학과 조회
    public List<Department> readDepartment() {
        List<Department> departmentList = departmentRepository.findAll();
        return departmentList;
    }

    // 학과 삭제
    public void deleteDepartment(Long deptId) {
        departmentRepository.deleteById(deptId);
    }

    // 학과 수정
    @Transactional
    public void updateDepartment(Long id, DepartmentFormDto departmentFormDto) {

        // 기존 학과 엔티티 가져오기
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new CustomRestfullException("해당 학과를 찾을 수 없습니다.",HttpStatus.NOT_FOUND, "/break/appList"));

        // 학과 이름 수정
        department.setName(departmentFormDto.getName());

        // 단과대 수정
        College college = collegeRepository.findById(departmentFormDto.getCollegeId())
                .orElseThrow(()-> new CustomRestfullException("단과대를 찾을 수 없습니다.",HttpStatus.NOT_FOUND, "/break/appList"));
        department.setCollege(college);

        departmentRepository.save(department); // 없어도됨 ? -> 트랜잭션 끝날 때 JPA가 변경 내용을 자동으로 감지(dirty checking)하고 UPDATE

    }

    // 단과대별 등록금 입력
    @Transactional
    public void createCollTuit(@Validated CollTuitFormDto collTuitFormDto) {
        // 단과대 엔티티 조회
        College college = collegeRepository.findById(collTuitFormDto.getCollegeId())
                .orElseThrow(() -> new CustomRestfullException("단과대를 찾을 수 없습니다.",HttpStatus.NOT_FOUND, "/break/appList"));

        //이미 해당 단과대에 등록금이 있는지 중복 체크
        if(collTuitRepository.existsByCollege_Id(collTuitFormDto.getCollegeId())){
            throw new CustomRestfullException("이미 등록금이 입력된 단과대 입니다.",HttpStatus.INTERNAL_SERVER_ERROR, "/break/appList");
        }

        //collTuit 엔티티 값 입력
        CollTuit collTuit = new CollTuit();
        collTuit.setCollege(college);
        collTuit.setAmount(collTuitFormDto.getAmount());
        collTuitRepository.save(collTuit);

    }

    //단과대 등록금 조회
    public List<CollTuitFormDto> readCollTuit() {

        //엔티티 전체 조회
        List<CollTuit> collTuitList = collTuitRepository.findAll();

        // 엔티티 -> DTO 변환 (새로운 private 메서드 없이, 여기서 바로)
        return collTuitList.stream().map(collTuit -> {
            CollTuitFormDto dto = new CollTuitFormDto();
            dto.setCollegeId(collTuit.getCollege().getId());
            dto.setName(collTuit.getCollege().getName()); // 단과대 이름
            dto.setAmount(collTuit.getAmount());
            return dto;
        }).toList();
    }

    // 단과대 등록금 삭제
    public void deleteCollTuit(Long collegeId) {
        collTuitRepository.deleteById(collegeId);

        // 없는 데이터 삭제 시 에러 처리
        if (!collTuitRepository.existsByCollege_Id(collegeId)) {
            throw new CustomRestfullException("해당 단과대의 등록금 정보가 없습니다.",
                    HttpStatus.NOT_FOUND, "/break/appList");
        }

        collTuitRepository.deleteByCollege_Id(collegeId);
    }

    //단과대 등록금 수정
    public void updateCollTuit(Long id, CollTuitFormDto collTuitFormDto) {

        // 기존 등록금 엔티티 조회
        CollTuit collTuit = collTuitRepository.findByCollege_Id(id)
                .orElseThrow(() ->
                        new CustomRestfullException("해당 단과대의 등록금 정보가 없습니다.",
                                HttpStatus.NOT_FOUND, "/break/appList"));

        // 값 변경
        collTuit.setAmount(collTuitFormDto.getAmount());
    }

	/**
	 * 강의실 입력 서비스
	 */
	@Transactional
	public void createRoom(@Validated RoomFormDto roomFormDto) {
		// 강의실 중복 입력 검사
		List<Room> roomList = roomRepository.findAll();
        for (Room value : roomList) {
            if (value.getId().equals((roomFormDto.getId()))) {
                throw new CustomRestfullException("이미 존재하는 강의실입니다", HttpStatus.INTERNAL_SERVER_ERROR, "/break/appList");
            }
        }

        Room room = new Room();
        room.setId(roomFormDto.getId());
        room.setCollege(roomFormDto.getCollege());

		roomRepository.save(room);
	}

	/**
	 * 강의실 조회 서비스
	 */
	public List<Room> readRoom() {
        return roomRepository.findAll();
	}

    /**
     * 강의실 삭제 서비스
     */
    public void deleteRoom(String id) {
        roomRepository.deleteById(id);
    }

    /**
     * 강의 입력 서비스
     */
    @Transactional
    public void createSubjectAndSyllabus(@Valid @RequestBody SubjectFormDto subjectFormDto) {
        // 강의실, 강의시간 중복 검사
        System.out.println("subjectFormDto = " + subjectFormDto);
        List<Subject> subjectList = subjectRepository.findByRoom_IdAndSubDayAndSubYearAndSemester(
                subjectFormDto.getRoomId(), subjectFormDto.getSubDay(), subjectFormDto.getSubYear(), subjectFormDto.getSemester());
        System.out.println("subjectList = " + subjectList);
        if (subjectList != null) {
            SubjectUtil subjectUtil = new SubjectUtil();
            boolean result = subjectUtil.calculate(subjectFormDto, subjectList);
            if (!result) {
                throw new CustomRestfullException("해당 시간대는 강의실을 사용중입니다! 다시 선택해주세요", HttpStatus.BAD_REQUEST, "/break/appList");
            }
        }
        Professor professor = professorRepository.findById(subjectFormDto.getProfessorId()).orElseThrow(
                () -> new CustomRestfullException("해당 교수 ID가 존재하지 않습니다", HttpStatus.NOT_FOUND, "/break/appList")
        );
        Room room = roomRepository.findById(subjectFormDto.getRoomId()).orElseThrow(
                () -> new CustomRestfullException("해당 강의실이 존재하지 않습니다.", HttpStatus.NOT_FOUND, "/break/appList")
        );
        Department department = departmentRepository.findById(subjectFormDto.getDeptId()).orElseThrow(
                () -> new CustomRestfullException("해당 학과가 존재하지 않습니다.", HttpStatus.NOT_FOUND, "/break/appList")
        );
        // 과목 추가 하고, 그 과목 키로 강의 계획서 생성(내용은 없음, 회원가입과 비슷한 느낌?)
        Subject subject = new Subject();
        subject.setName(subjectFormDto.getName());
        subject.setProfessor(professor);
        subject.setRoom(room);
        subject.setDepartment(department);
        subject.setType(subjectFormDto.getType());
        subject.setSubYear(subjectFormDto.getSubYear());
        subject.setSemester(subjectFormDto.getSemester());
        subject.setSubDay(subjectFormDto.getSubDay());
        subject.setStartTime(subjectFormDto.getStartTime());
        subject.setEndTime(subjectFormDto.getEndTime());
        subject.setGrades(subjectFormDto.getGrades());
        subject.setCapacity(subjectFormDto.getCapacity());
        subject.setNumOfStudent(subjectFormDto.getNumOfStudent());
        subjectRepository.save(subject);

        Long subjectId = subject.getId();

        SyllaBus syllaBus = new SyllaBus(); // 강의 아이디로 강의 찾아 엔티티에 강의만 저장함
        syllaBus.setSubject(subjectRepository.findById(subjectId).orElseThrow(
                () -> new CustomRestfullException("해당 과목을 찾을 수 없습니다.", HttpStatus.NOT_FOUND, "/break/appList")));
		syllaBusRepository.save(syllaBus);
    }

	/**
	 * 강의 조회 서비스
	 */
	public List<Subject> readSubject() {
        return subjectRepository.findAll();
	}

	/**
	 * 강의 삭제 서비스
	 */
	public void deleteSubject(Long id) {
		subjectRepository.deleteById(id);
		syllaBusRepository.deleteById(id);
	}

	/**
	 * 강의 수정 서비스
	 */
	public void updateSubject(Long id, SubjectFormDto subjectFormDto) {
		// ID로 연도 학기 조회
		Subject subject = subjectRepository.findById(id).orElseThrow(
                () -> new CustomRestfullException("해당 과목을 찾을 수 없습니다.", HttpStatus.NOT_FOUND, "/break/appList")
        );
		// 강의실, 강의시간 중복 검사
		List<Subject> subjectList = subjectRepository.findByRoom_IdAndSubDayAndSubYearAndSemester(
                subject.getRoom().getId(), subject.getSubDay(), subject.getSubYear(), subject.getSemester()
        );
		if (subjectList != null) {
			SubjectUtil subjectUtil = new SubjectUtil();
			boolean result = subjectUtil.calculate(subjectFormDto, subjectList);
			if (!result) {
				throw new CustomRestfullException("해당 시간대는 강의실을 사용중입니다! 다시 선택해주세요", HttpStatus.BAD_REQUEST, "/break/appList");
			}
		}
        subject.setName(subjectFormDto.getName());
        subject.setRoom(subject.getRoom());
        subject.setSubDay(subjectFormDto.getSubDay());
        subject.setStartTime(subjectFormDto.getStartTime());
        subject.setEndTime(subjectFormDto.getEndTime());
        subject.setNumOfStudent(subjectFormDto.getNumOfStudent());
        subjectRepository.save(subject);
	}

}
