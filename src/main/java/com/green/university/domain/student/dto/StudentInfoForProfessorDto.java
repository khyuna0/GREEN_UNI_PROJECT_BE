package com.green.university.domain.student.dto;

import com.green.university.domain.subject.entity.StuSubDetail;
import com.green.university.domain.student.entity.Student;
import lombok.Data;

/**
 * 과목에 대한 학생의 디테일한 정보(교수 조회, 입력용)
 * @author 김지현
 */
@Data
public class StudentInfoForProfessorDto {

	private Long id;
	private Long studentId;
	private String studentName;
	// 학생 소속
	private String deptName;
	// 결석 횟수
	private Long absent;
	// 지각 횟수
	private Long lateness;
	// 과제 점수
	private Long homework;
	// 중간고사 점수
	private Long midExam;
	// 기말고사 점수
	private Long finalExam;
	// 총합 환산 점수
	private Double convertedMark;
    // 등급 (단순확인용)
    private String grade;
    //위험학생 여부
    private String status;


	// StuSubDetail 엔티티 -> StudentInfoForProfessorDto로 변환
    public static StudentInfoForProfessorDto fromEntity(StuSubDetail s) {
        StudentInfoForProfessorDto dto = new StudentInfoForProfessorDto();

        Student student = s.getStudent();

        dto.setId(s.getId());
        dto.setStudentId(student.getId());
        dto.setStudentName(student.getName());
        dto.setDeptName(student.getDepartment().getName());

        dto.setAbsent(s.getAbsent());
        dto.setLateness(s.getLateness());
        dto.setHomework(s.getHomework());
        dto.setMidExam(s.getMildExam());        // 엔티티 컬럼명 mildExam 주의
        dto.setFinalExam(s.getFinalExam());
        dto.setConvertedMark(s.getConvertedMark());
        dto.setGrade(s.getGrade());
        if(s.getGrade() != null) {
            dto.setStatus(s.getGrade());
        }

        return dto;
    }
    private void setStatus (String grade) {
       if (grade.contains("C0")) {
            this.status =  "경고";
       }
        if (grade.contains("D") || grade.contains("F")) {
            this.status =  "위험";
        }
    }
	
}
