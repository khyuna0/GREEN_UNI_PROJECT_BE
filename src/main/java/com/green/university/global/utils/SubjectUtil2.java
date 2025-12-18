package com.green.university.global.utils;

import com.green.university.domain.subject.dto.SubjectFormDto;
import com.green.university.domain.subject.entity.Subject;
import com.green.university.global.exception.CustomRestfullException;
import org.springframework.http.HttpStatus;

import java.util.List;

public class SubjectUtil2 {


    /**
     *  시작시간/종료시간 유효성 검사
     *  시작 < 종료가 아니면 저장/수정 막기
     *  시작 == 종료도 막음
     */
    public static void validateTimeRange(SubjectFormDto dto) {
        if (dto == null) {
            throw new CustomRestfullException("요청 데이터가 비어있습니다.", HttpStatus.BAD_REQUEST);
        }
        Long start = dto.getStartTime();
        Long end = dto.getEndTime();

        if (start == null || end == null) {
            throw new CustomRestfullException("시작시간/종료시간은 필수입니다.", HttpStatus.BAD_REQUEST);
        }

        if (start >= end) {
            throw new CustomRestfullException("종료시간은 시작시간보다 늦어야 합니다.", HttpStatus.BAD_REQUEST);
        }
    }


    // 겹침 검사
    // 겹침 = (신규 시작 < 기존 종료) AND (신규 종료 > 기존 시작)
    public static boolean isNotOverlapped(SubjectFormDto dto, List<Subject> subjectList) {
        for (Subject s : subjectList) {
            long existStart = s.getStartTime();
            long existEnd = s.getEndTime();
            long newStart = dto.getStartTime();
            long newEnd = dto.getEndTime();

            boolean overlapped = (newStart < existEnd) && (newEnd > existStart);
            if (overlapped) return false;
        }
        return true;
    }
}
