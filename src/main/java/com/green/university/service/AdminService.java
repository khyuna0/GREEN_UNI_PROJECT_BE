package com.green.university.service;

import com.green.university.dto.*;
import com.green.university.entity.*;
import com.green.university.handler.exception.CustomRestfullException;
import com.green.university.repository.interfaces.*;
import com.green.university.utils.SubjectUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

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

    /**
     * 단과대 입력 서비스
     */
    @Transactional
    public void createCollege(@Validated CollegeFormDto collegeFormDto) {
        // 같은 이름 중복 검사
        List<College> collegeList = collegeRepository.selectCollegeDto();
        for (int i = 0; i < collegeList.size(); i++) {
            if (collegeList.get(i).getName().equals(collegeFormDto.getName())) {
                throw new CustomRestfullException("이미 존재하는 단과대입니다", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        Long resultRowCount = collegeRepository.insert(collegeFormDto);
        if (resultRowCount != 1) {
            System.out.println("단과대 입력 서비스 오류");
        }
    }

    /**
     * 단과대 조회 서비스
     */
    @Transactional
    public List<College> readCollege() {
        List<College> collegeList = collegeRepository.selectCollegeDto();
        return collegeList;
    }

    /**
     * 단과대 삭제 서비스
     */
    public void deleteCollege(Long id) {
        collegeRepository.deleteById(id);
    }

    /**
     * 학과 입력 서비스
     */
    @Transactional
    public void createDepartment(@Validated DepartmentFormDto departmentFormDto) {

        // 같은 학과 이름이 이미 존재하는지 검사
        if (departmentRepository.existsByName(departmentFormDto.getName())) {
            throw new CustomRestfullException("이미 존재하는 학과입니다", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        Department department = new Department();
        department.setName(departmentFormDto.getName());

        //collegeId 까지 매핑?
        College college = collegeRepository.findById(departmentFormDto.getCollegeId())
                .orElseThrow(() -> new CustomRestfullException("단과대를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        department.setCollege(college);

        departmentRepository.save(department);

    }

    // 학과 조회
    public List<Department> readDepartment() {
        List<Department> departmentList = departmentRepository.findAll();
        return departmentList;
    }

    // 학과 삭제
    public void deleteDepartment(Long collegeId) {
        departmentRepository.deleteById(collegeId);
    }

   // 학과 수정
    public void updateDepartment(DepartmentFormDto departmentFormDto) {

        // 기존 학과 엔티티 가져오기
        Department department = departmentRepository.findById(departmentFormDto.getId())
                .orElseThrow(() -> new CustomRestfullException("해당 학과를 찾을 수 없습니다.",HttpStatus.NOT_FOUND));

        // 학과 이름 수정
        department.setName(departmentFormDto.getName());

        // 단과대 수정
        College college =


    }

    // 단과대별 등록금 입력
    @Transactional
    public void createCollTuit(@Validated CollTuitFormDto collTuitFormDto) {
        // 단과대 엔티티 조회
        College college = collegeRepository.findById(collTuitFormDto.getCollegeId())
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
    public void deleteCollTuit(Long collegeId) {
        collTuitRepository.deleteById(collegeId);

        // 없는 데이터 삭제 시 에러 처리
        if (!collTuitRepository.existsByCollege_Id(collegeId)) {
            throw new CustomRestfullException("해당 단과대의 등록금 정보가 없습니다.",
                    HttpStatus.NOT_FOUND);
        }

        collTuitRepository.deleteByCollege_Id(collegeId);
    }

    //단과대 등록금 수정
    public void updateCollTuit(CollTuitFormDto collTuitFormDto) {

        // 기존 등록금 엔티티 조회
        CollTuit collTuit = collTuitRepository.findByCollege_Id(collTuitFormDto.getCollegeId())
                .orElseThrow(() ->
                        new CustomRestfullException("해당 단과대의 등록금 정보가 없습니다.",
                                HttpStatus.NOT_FOUND));

        // 값 변경
        collTuit.setAmount(collTuitFormDto.getAmount());
    }

    /**
     * 강의실 입력 서비스
     */
    @Transactional
    public void createRoom(@Validated RoomFormDto roomFormDto) {
        // 강의실 중복 입력 검사
        List<Room> roomList = roomRepository.selectByRoomDto();
        for (int i = 0; i < roomList.size(); i++) {
            if (roomList.get(i).getId().equals((roomFormDto.getId()))) {
                throw new CustomRestfullException("이미 존재하는 강의실입니다", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        Long resultRowCount = roomRepository.insert(roomFormDto);
        if (resultRowCount != 1) {
            System.out.println("강의실 입력 서비스 오류");
        }
    }

    /**
     * 강의실 조회 서비스
     */
    public List<Room> readRoom() {
        List<Room> roomList = roomRepository.selectByRoomDto();
        return roomList;
    }

    /**
     * 강의실 삭제 서비스
     */
    public void deleteRoom(Long id) {
        roomRepository.deleteById(id);
    }

    /**
     * 강의 입력 서비스
     */
    @Transactional
    public List<Subject> createSubjectAndSyllabus(@Validated SubjectFormDto subjectFormDto) {
        // 강의실, 강의시간 중복 검사
        List<Subject> subjectList = subjectRepository.selectByRoomIdAndSubDayAndSubYearAndSemester(subjectFormDto);
        if (subjectList != null) {
            SubjectUtil subjectUtil = new SubjectUtil();
            boolean result = subjectUtil.calculate(subjectFormDto, subjectList);
            if (result == false) {
                throw new CustomRestfullException("해당 시간대는 강의실을 사용중입니다! 다시 선택해주세요", HttpStatus.BAD_REQUEST);
            }
        }
        subjectRepository.insert(subjectFormDto);

        // 강의계획서에 강의 ID 저장
        Long subjectId = subjectRepository.selectIdOrderById(subjectFormDto);
        syllaBusRepository.insertOnlySubId(subjectId);
        return subjectList;
    }

    /**
     * 강의 조회 서비스
     */
    public List<Subject> readSubject() {
        List<Subject> subjectList = subjectRepository.selectAll();
        return subjectList;
    }

    /**
     * 강의 삭제 서비스
     */
    public void deleteSubject(Long id) {
        subjectRepository.deleteById(id);
        syllaBusRepository.delete(id);
    }

    /**
     * 강의 수정 서비스
     */
    public Long updateSubject(SubjectFormDto subjectFormDto) {
        // ID로 연도 학기 조회
        Subject subject = subjectRepository.selectSubjectById(subjectFormDto.getId());
        subjectFormDto.setSubYear(subject.getSubYear());
        subjectFormDto.setSemester(subject.getSemester());
        // 강의실, 강의시간 중복 검사
        List<Subject> subjectList = subjectRepository.selectByRoomIdAndSubDayAndSubYearAndSemester(subjectFormDto);
        if (subjectList != null) {
            SubjectUtil subjectUtil = new SubjectUtil();
            boolean result = subjectUtil.calculate(subjectFormDto, subjectList);
            if (result == false) {
                throw new CustomRestfullException("해당 시간대는 강의실을 사용중입니다! 다시 선택해주세요", HttpStatus.BAD_REQUEST);
            }
        }
        Long resultRowCount = subjectRepository.updateBySubjectDto(subjectFormDto);
        if (resultRowCount != 1) {
            System.out.println("강의 수정 서비스 오류");
        }
        return resultRowCount;
    }

}
