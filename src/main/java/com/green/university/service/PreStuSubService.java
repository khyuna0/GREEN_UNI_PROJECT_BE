package com.green.university.service;

import com.green.university.dto.response.StuSubAppDto;
import com.green.university.dto.response.StuSubDayTimeDto;
import com.green.university.dto.response.StuSubSumGradesDto;
import com.green.university.entity.Student;
import com.green.university.handler.exception.CustomRestfullException;
import com.green.university.repository.interfaces.PreStuSubRepository;
import com.green.university.repository.interfaces.StudentRepository;
import com.green.university.repository.interfaces.SubjectRepository;
import com.green.university.entity.PreStuSub;
import com.green.university.entity.Subject;
import com.green.university.utils.Define;
import com.green.university.utils.StuSubUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author 서영
 *
 */
@Service
public class PreStuSubService {

	@Autowired
	private PreStuSubRepository preStuSubRepository;

	@Autowired
	private SubjectRepository subjectRepository;

	@Autowired
	private SubjectService subjectService;

    @Autowired
    private StudentRepository studentRepository;

	// 학생의 예비 수강신청 내역에 해당 강의가 존재하는지 확인
	public PreStuSub readPreStuSub(Long studentId, Long subjectId) {

		PreStuSub preStuSubEntity = preStuSubRepository.findByStudent_IdAndSubject_Id(studentId, subjectId);

		return preStuSubEntity;
	}

	// 학생의 전체 예비 수강신청 내역 조회
	public List<StuSubAppDto> readPreStuSubList(Long studentId) {

		List<StuSubAppDto> preStuSubList = preStuSubRepository.findByStudentIdAndSemester(studentId,
				Define.CURRENT_YEAR, Define.CURRENT_SEMESTER);

		return preStuSubList;
	}

	// 학생의 예비 수강신청 내역 추가
	@Transactional
	public void createPreStuSub(Long studentId, Long subjectId) {

		// 신청 대상 과목 정보
		Subject targetSubject = subjectRepository.findById(subjectId).orElseThrow(() -> new CustomRestfullException("없는 과목입니다.", HttpStatus.NOT_FOUND));

		// 현재 총 신청 학점
		StuSubSumGradesDto stuSubSumGradesDto = preStuSubRepository.selectSumGrades(studentId, Define.CURRENT_YEAR,
				Define.CURRENT_SEMESTER);

		// 최대 수강 가능 학점을 넘지 않는지 확인
		StuSubUtil.checkSumGrades(targetSubject, stuSubSumGradesDto);

		// 해당 학생의 예비 수강 신청 내역 시간표
		List<StuSubDayTimeDto> dayTimeList = preStuSubRepository.selectDayTime(studentId);

		// 현재 학생의 시간표와 겹치지 않는지 확인
		StuSubUtil.checkDayTime(targetSubject, dayTimeList);

		// 수강신청 내역 추가
        PreStuSub preStuSub = new PreStuSub();
        preStuSub.setStudent(studentRepository.findById(studentId).orElseThrow(() -> new CustomRestfullException("없는 학생 정보입니다.", HttpStatus.NOT_FOUND)));
        preStuSub.setSubject(subjectRepository.findById(subjectId).orElseThrow(() -> new CustomRestfullException("없는 과목입니다.", HttpStatus.NOT_FOUND)));
        preStuSubRepository.save(preStuSub);

		// 해당 강의 현재인원 +1
        Subject subject = subjectRepository.findById(subjectId).orElseThrow(() -> new CustomRestfullException("없는 과목입니다.", HttpStatus.NOT_FOUND));
		subject.setNumOfStudent(subject.getNumOfStudent()+1);

	}

	// 학생의 예비 수강신청 내역 삭제
	@Transactional
	public void deletePreStuSub(Long studentId, Long subjectId) {

        PreStuSub preStuSub = preStuSubRepository.findByStudent_IdAndSubject_Id(studentId, subjectId );
		// 해당 강의 현재인원 -1
        Subject subject = subjectRepository.findById(subjectId).orElseThrow(() -> new CustomRestfullException("없는 과목입니다.", HttpStatus.NOT_FOUND));
        subject.setNumOfStudent(subject.getNumOfStudent()-1);

	}

}
