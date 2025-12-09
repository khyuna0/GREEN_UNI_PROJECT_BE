package com.green.university.repository;

import com.green.university.entity.Subject;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject,Long>,
        JpaSpecificationExecutor<Subject> {

    // 강의 입력 시 같은 강의실, 요일, 연도, 학기 정보 조회
    List<Subject> findByRoom_IdAndSubDayAndSubYearAndSemester(String room_id, String subDay, Long subYear, Long semester);

    // 제일 최근 강의 ID 조회 (필요한 메서드?)
    Long findIdOrderById(Long id);

    // 수강 신청에 사용할 강의 정보 (아래 페이지 메서드 이용 할 것)
    List<Subject> findBySubYearAndSemester(Long subYear, Long semester);
    Page<Subject> findBySubYearAndSemester(Long subYear, Long semester, Pageable pageable);
    // 전체 강의 정보
    Page<Subject> findAll(Pageable pageable);

    // 교수 본인의 수업이 있는 년도-학기
    List<Subject> findByProfessor_Id(Long professorId);

    // 년도와 학기, 교수 id를 이용하여 해당 과목의 정보 불러오기 (SubjectPeriodForProfessorDto)
    List<Subject> findByProfessor_IdAndSubYearAndSemester(Long professorId, Long subYear, Long semester);

    // 페이징 필요한가?
    // 연도-학기-개설학과-강의명 검색을 조건으로 한 강의 정보
    List<Subject> findBySubYearAndSemesterAndDepartment_IdAndNameContaining(
            Long subYear, Long semester, Long deptId, String name);
    // 연도-학기-강의구분-개설학과-강의명 검색을 조건으로 한 강의 정보 (currentSemesterSubjectSearchFormDto)
    List<Subject> findBySubYearAndSemesterAndTypeAndDepartment_IdAndNameContaining(
            Long subYear, Long semester, String type, Long deptId, String name);

    // 정원 >= 신청인원인 강의의 id 리스트
    @Query("select s.id from Subject s where s.capacity >= s.numOfStudent")
    List<Long> findIdByCapacityGreaterThanOrEqualNumOfStudent();

    // 정원 < 신청인원인 강의의 id 리스트
    @Query("select s.id from Subject s where s.capacity < s.numOfStudent")
    List<Long> findIdByCapacityLessThanNumOfStudent();


}
