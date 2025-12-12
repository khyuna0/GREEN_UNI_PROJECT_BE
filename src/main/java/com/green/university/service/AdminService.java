package com.green.university.service;

import com.green.university.dto.*;
import com.green.university.entity.*;
import com.green.university.exception.CustomRestfullException;
import com.green.university.repository.*;
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

    /**
     *
     * 단과대
     */

    // 단과대 입력
    @Transactional
    public void createCollege(@Validated CollegeFormDto collegeFormDto) {
        // 같은 이름 중복 검사
        if(collegeRepository.existsByName(collegeFormDto.getName())){
            throw new CustomRestfullException("이미 존재하는 단과대입니다", HttpStatus.INTERNAL_SERVER_ERROR);
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
    @Transactional
    public void deleteCollege(Long collegeId) {

        if (!collegeRepository.existsById(collegeId)) {
            throw new CustomRestfullException("해당 단과대를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
        }
        collegeRepository.deleteById(collegeId);
    }

    //단과대 수정
    @Transactional
    public void updateCollege(Long collegeId, CollegeFormDto collegeFormDto) {

        // 기존 단과대 엔티티 조회
        College college = collegeRepository.findById(collegeId)
                .orElseThrow(() ->
                        new CustomRestfullException("해당 단과대를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        String beforeName = college.getName();
        String afterName  = collegeFormDto.getName();

        // 이름이 실제로 변경될 때 중복 체크
        if (!beforeName.equals(afterName) && collegeRepository.existsByName(afterName)) {
            throw new CustomRestfullException("이미 존재하는 단과대입니다.", HttpStatus.BAD_REQUEST);
        }
        college.setName(afterName);
    }

    /**
     *
     * 학과
     */

    // 학과 입력
    @Transactional
    public void createDepartment(@Validated DepartmentFormDto departmentFormDto) {

        // 같은 학과 이름이 이미 존재하는지 검사
        if (departmentRepository.existsByName(departmentFormDto.getName())) {
            throw new CustomRestfullException("이미 존재하는 학과입니다", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        Department department = new Department();
        department.setName(departmentFormDto.getName());

        // 단과대 이름으로 조회
        College college = collegeRepository.findByName(departmentFormDto.getCollegeName())
                .orElseThrow(() -> new CustomRestfullException("단과대를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        department.setCollege(college);

        departmentRepository.save(department);
    }

    // 학과 조회
    public List<Department> readDepartment() {
        return departmentRepository.findAllByOrderByCollege_IdAsc();
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
                .orElseThrow(() -> new CustomRestfullException("해당 학과를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        // 학과 이름 수정
        department.setName(departmentFormDto.getName());

        // 단과대 수정
        College college = collegeRepository.findByName(departmentFormDto.getCollegeName())
                .orElseThrow(()-> new CustomRestfullException("단과대를 찾을 수 없습니다.",HttpStatus.NOT_FOUND));
        department.setCollege(college);

        departmentRepository.save(department); // 없어도됨 ? -> 트랜잭션 끝날 때 JPA가 변경 내용을 자동으로 감지(dirty checking)하고 UPDATE

    }

    /**
     *
     * 단과대 등록금
     */

    // 단과대별 등록금 입력
    @Transactional
    public void createCollTuit(@Validated CollTuitFormDto collTuitFormDto) {
        // 단과대 이름으로 엔티티 조회
        College college = collegeRepository.findByName(collTuitFormDto.getName())
                .orElseThrow(() -> new CustomRestfullException("단과대를 찾을 수 없습니다.",HttpStatus.NOT_FOUND));

        //이미 해당 단과대에 등록금이 있는지 중복 체크
        if(collTuitRepository.existsByCollege_Id(collTuitFormDto.getCollegeId())){
            throw new CustomRestfullException("이미 등록금이 입력된 단과대 입니다.",HttpStatus.INTERNAL_SERVER_ERROR);
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
    @Transactional
    public void deleteCollTuit(Long collegeId) {

        if (!collTuitRepository.existsByCollege_Id(collegeId)) {
            throw new CustomRestfullException(
                    "해당 단과대의 등록금 정보가 없습니다.",
                    HttpStatus.NOT_FOUND
            );
        }


        collTuitRepository.deleteByCollege_Id(collegeId);
    }

    // 단과대 등록금 수정
    @Transactional
    public void updateCollTuit(Long collegeId, CollTuitFormDto collTuitFormDto) {
        CollTuit collTuit = collTuitRepository.findByCollege_Id(collegeId)
                .orElseThrow(() ->
                        new CustomRestfullException("해당 단과대의 등록금 정보가 없습니다.",
                                HttpStatus.NOT_FOUND));

        collTuit.setAmount(collTuitFormDto.getAmount());
    }


    /**
     *
     * 강의실
     */

    // 강의실 조회
    public List<Room> readRoom() {
        return roomRepository.findAll();
    }

    // 강의실 등록
    @Transactional
    public void createRoom(RoomFormDto roomFormDto) {
        List<Room> roomList = roomRepository.findAll();
        for (Room value : roomList) {
            if (value.getId().equals((roomFormDto.getId()))) {
                throw new CustomRestfullException("이미 존재하는 강의실입니다", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        College college = collegeRepository.findById(roomFormDto.getCollegeId()).orElseThrow(
                () -> new CustomRestfullException("존재하지 않는 단과대입니다.", HttpStatus.BAD_REQUEST));
        Room room = new Room();
        room.setId(roomFormDto.getId());
        room.setCollege(college);
        roomRepository.save(room);
    }

    // 강의실 삭제
    public void deleteRoom(String id) {

        if (subjectRepository.existsByRoom_Id(id)) {
            throw new CustomRestfullException(
                    "해당 강의실을 사용하는 강의가 있어 삭제할 수 없습니다.",
                    HttpStatus.BAD_REQUEST
            );
        }

        roomRepository.deleteById(id);
    }


    // 강의실 수정
    @Transactional
    public void updateRoom(String id, RoomFormDto roomFormDto){
        // 기존 강의실 조회
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new CustomRestfullException("해당 강의실이 없습니다.", HttpStatus.NOT_FOUND));

        // 단과대 재매핑
        College college = collegeRepository.findById(roomFormDto.getCollegeId())
                .orElseThrow(() -> new CustomRestfullException("존재하지 않는 단과대입니다.", HttpStatus.BAD_REQUEST));

        room.setCollege(college);
    }


    /**
     *
     *  강의
     */

    // 강의 조회
    public List<Subject> readSubject() {
        return subjectRepository.findAll();
    }


    // 강의 입력
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
                throw new CustomRestfullException("해당 시간대는 강의실을 사용중입니다! 다시 선택해주세요", HttpStatus.BAD_REQUEST);
            }
        }
        Professor  professor = professorRepository.findByName(subjectFormDto.getProfessorName())
                .orElseThrow(() -> new CustomRestfullException("해당 교수 이름이 존재하지 않습니다", HttpStatus.NOT_FOUND));

        Department  department = departmentRepository.findByName(subjectFormDto.getDeptName())
                .orElseThrow(() -> new CustomRestfullException("해당 학과 이름이 존재하지 않습니다.", HttpStatus.NOT_FOUND));
        Room room = roomRepository.findById(subjectFormDto.getRoomId()).orElseThrow(
                () -> new CustomRestfullException("해당 강의실이 존재하지 않습니다.", HttpStatus.NOT_FOUND)
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
        subject.setCapacity(subjectFormDto.getCapacity());
        subjectRepository.save(subject);

        Long subjectId = subject.getId();

        Syllabus syllabus = new Syllabus(); // 강의 아이디로 강의 찾아 엔티티에 강의만 저장함
        syllabus.setSubject(subjectRepository.findById(subjectId).orElseThrow(
                () -> new CustomRestfullException("해당 과목을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)));
        syllaBusRepository.save(syllabus);
    }

    // 강의 삭제
    public void deleteSubject(Long id) {
        subjectRepository.deleteById(id);
        syllaBusRepository.deleteById(id);
    }

    // 강의 수정
    @Transactional
    public void updateSubject(Long id, SubjectFormDto subjectFormDto) {
        // ID로 연도 학기 조회
        Subject subject = subjectRepository.findById(id).orElseThrow(
                () -> new CustomRestfullException("해당 과목을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        );
        // 강의실, 강의시간 중복 검사
        List<Subject> subjectList = subjectRepository.findByRoom_IdAndSubDayAndSubYearAndSemester(
                subject.getRoom().getId(), subject.getSubDay(), subject.getSubYear(), subject.getSemester()
        );
        if (subjectList != null) {
            SubjectUtil subjectUtil = new SubjectUtil();
            boolean result = subjectUtil.calculate(subjectFormDto, subjectList);
            if (!result) {
                throw new CustomRestfullException("해당 시간대는 강의실을 사용중입니다! 다시 선택해주세요", HttpStatus.BAD_REQUEST);
            }
        }
        subject.setName(subjectFormDto.getName());
        subject.setRoom(subject.getRoom());
        subject.setSubDay(subjectFormDto.getSubDay());
        subject.setStartTime(subjectFormDto.getStartTime());
        subject.setEndTime(subjectFormDto.getEndTime());
        subjectRepository.save(subject);
    }

}
