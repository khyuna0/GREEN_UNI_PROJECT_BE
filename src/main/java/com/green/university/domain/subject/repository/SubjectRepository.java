package com.green.university.domain.subject.repository;

import com.green.university.domain.subject.entity.Subject;
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

    // 전체 강의 정보
    Page<Subject> findAll(Pageable pageable);

    // 교수 본인의 수업이 있는 년도-학기
    List<Subject> findByProfessor_Id(Long professorId);

    // 년도와 학기, 교수 id를 이용하여 해당 과목의 정보 불러오기 (SubjectPeriodForProfessorDto)
    List<Subject> findByProfessor_IdAndSubYearAndSemester(Long professorId, Long subYear, Long semester);

    // 정원 >= 신청인원인 강의의 id 리스트
    @Query("select s.id from Subject s where s.capacity >= s.numOfStudent")
    List<Long> findIdByCapacityGreaterThanOrEqualNumOfStudent();

    // 정원 < 신청인원인 강의의 id 리스트
    @Query("select s.id from Subject s where s.capacity < s.numOfStudent")
    List<Long> findIdByCapacityLessThanNumOfStudent();

    // 강의의 총 수강 학생 수
    @Query("SELECT s.numOfStudent FROM Subject s WHERE s.id = :subjectId")
    int findNumOfStudentById(Long subjectId);

    // 해당 강의실을 사용하는 강의가 하나라도 있는지 체크
    boolean existsByRoom_Id(String roomId);

}
