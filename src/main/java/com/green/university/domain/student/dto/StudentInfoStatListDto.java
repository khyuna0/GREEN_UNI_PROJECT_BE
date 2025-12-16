package com.green.university.domain.student.dto;

import com.green.university.domain.breakapp.entity.BreakApp;
import lombok.Data;

import java.time.LocalDate;

// 학생의 my info 탭에 표시할 학적변동 리스트 받을 dto
@Data
public class StudentInfoStatListDto {

    // 변동 날짜
    private LocalDate fromDate;
    // 변동 구분
    private String status;
    // 변동 세부 구분 (type : 질병, 출산, 군 ..)
    private String detail;
    // 승인 여부
    private String adopt;
    // 복학 예정 년도
    private Long toYear;
    // 복학 예정 학기
    private Long toSemester;


    public static StudentInfoStatListDto fromEntity(StuStat stuStat) {
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
        return dto;
    }
}


