package com.green.university.global.utils;

import com.green.university.domain.subject.dto.SubjectFormDto;
import com.green.university.domain.subject.entity.Subject;

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
 *
 * 기존 시작 9시 <= 신규 시작 13시 && 신규 시작 13시 < 기존 종료 11시
 * || 기존 시작 9시 < 신규 종료 15시 && 신규 종료 15시 <= 기존 종료 11시
 * -> false 리턴
 *
 *  기존 강의가 만약에 월요일 9시부터 11시
 *  수정 강의가 만약에 월요일 10시부터 12시
 *
 * 기존 시작 9시 <= 신규 시작 10시 && 신규 시작 10시 < 기존 종료 11시
 * || 기존 시작 9시 < 신규 종료 12시 && 신규 종료 12시 <= 기존 종료 11시
 * -> true 리턴
 *
 * true || true -> true
 * true || false -> true
 * false || true -> true
 * false || false -> false
 *
 *
 * */
