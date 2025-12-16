package com.green.university.domain.subject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TimetableCourseDto {
    private Long subjectId;
    private String subjectName;
    private String subDay;
    private Long startTime;
    private Long endTime;
    private String roomId;
    private String professorName;
    private Long credits;

    // 변환용
    public static TimetableCourseDto from(StuSubAppDto s) {
        return new TimetableCourseDto(
                s.getSubjectId(),
                s.getSubjectName(),
                s.getSubDay(),
                s.getStartTime(),
                s.getEndTime(),
                s.getRoomId(),
                s.getProfessorName(),
                s.getCredits()
        );
    }
}
