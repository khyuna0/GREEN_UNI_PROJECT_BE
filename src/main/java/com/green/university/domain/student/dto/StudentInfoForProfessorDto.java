package com.green.university.domain.student.dto;

import com.green.university.domain.student.entity.Student;
import com.green.university.domain.subject.entity.StuSubDetail;
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
    private String letterGrade;
    //위험학생 여부
    private String status;
    // 해당 과목 finalized 여부
    private boolean finalized;


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
        dto.setMidExam(s.getMidExam());
        dto.setFinalExam(s.getFinalExam());
        dto.setConvertedMark(s.getConvertedMark());
        dto.setLetterGrade(s.getLetterGrade());
        if(s.getLetterGrade() != null) {
            dto.setStatus(s.getLetterGrade());
        }
        dto.setFinalized(s.isFinalized());
        return dto;
    }
    private void setStatus (String letterGrade) {
       if (letterGrade.contains("C0")) {
            this.status =  "경고";
       }
        if (letterGrade.contains("D") || letterGrade.contains("F")) {
            this.status =  "위험";
        }
    }
	
}
