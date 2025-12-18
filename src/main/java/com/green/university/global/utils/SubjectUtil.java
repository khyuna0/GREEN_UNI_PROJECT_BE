package com.green.university.global.utils;

import com.green.university.domain.subject.dto.SubjectFormDto;
import com.green.university.domain.subject.entity.Subject;
import com.green.university.global.exception.CustomRestfullException;
import org.springframework.http.HttpStatus;

import java.util.List;

/**
 *
 * @author 성희 강의 입력 관련 유틸
 *
 */
public class SubjectUtil {

    public boolean calculate(SubjectFormDto subjectFormDto, List<Subject> subjectList) {
        for (int i = 0; i < subjectList.size(); i++) {
            if ((subjectList.get(i).getStartTime() <= subjectFormDto.getStartTime()
                    && subjectFormDto.getStartTime() < subjectList.get(i).getEndTime())
                    || (subjectList.get(i).getStartTime() < subjectFormDto.getEndTime()
                    && subjectFormDto.getEndTime() <= subjectList.get(i).getEndTime())) {
                return false;
            }
        }
        return true;
    }
}

	/**
	 * 🍎 이해가 안 돼!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 * 기존 강의가 만약에 월요일 9시부터 11시
	 * 수정 강의가 만약에 월요일 13시부터 15시
	 * <p>
	 * 기존 시작 9시 <= 신규 시작 13시 && 신규 시작 13시 < 기존 종료 11시
	 * || 기존 시작 9시 < 신규 종료 15시 && 신규 종료 15시 <= 기존 종료 11시
	 * -> false 리턴
	 * <p>
	 * 기존 강의가 만약에 월요일 9시부터 11시
	 * 수정 강의가 만약에 월요일 10시부터 12시
	 * <p>
	 * 기존 시작 9시 <= 신규 시작 10시 && 신규 시작 10시 < 기존 종료 11시
	 * || 기존 시작 9시 < 신규 종료 12시 && 신규 종료 12시 <= 기존 종료 11시
	 * -> true 리턴
	 * <p>
	 * true || true -> true
	 * true || false -> true
	 * false || true -> true
	 * false || false -> false
	 */

// 시간 겹침 체크해서 겹치면 바로 예외 던짐
//	public void checkTimeConflict(SubjectFormDto newSubject, List<Subject> existingSubjects) {
//
//		for (Subject existing : existingSubjects) {
//			Long newStart = newSubject.getStartTime();
//			Long newEnd = newSubject.getEndTime();
//			Long existStart = existing.getStartTime();
//			Long existEnd = existing.getEndTime();
//
//			// 겹치는 경우를 직접 체크
//			// 1) 신규 시작이 기존 시간 안에 들어감
//			// 2) 신규 종료가 기존 시간 안에 들어감
//			// 3) 신규가 기존을 완전히 포함함
//			boolean case1 = (newStart >= existStart && newStart < existEnd);
//			boolean case2 = (newEnd > existStart && newEnd <= existEnd);
//			boolean case3 = (newStart <= existStart && newEnd >= existEnd);
//
//			if (case1 || case2 || case3) {
//				throw new CustomRestfullException(
//						"해당 시간대는 강의실을 사용중입니다! (기존: " + existStart + "~" + existEnd + "시)",
//						HttpStatus.BAD_REQUEST
//				);
//			}
//		}
//		// 여기까지 오면 = 겹침 없음
//	}
//}
