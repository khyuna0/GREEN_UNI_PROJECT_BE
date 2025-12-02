package com.green.university.service;

import com.green.university.entity.BreakApp;
import com.green.university.entity.Student;
import com.green.university.handler.exception.CustomRestfullException;
import com.green.university.repository.interfaces.BreakAppRepository;
import com.green.university.repository.interfaces.StuStatRepository;
import com.green.university.repository.interfaces.StudentRepository;
import com.green.university.entity.StuStat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * @author 서영
 *
 */
@Service
public class StuStatService {

	@Autowired
	private StuStatRepository stuStatRepository;

	@Autowired
	private StudentRepository studentRepository;
    @Autowired
    private BreakAppRepository breakAppRepository;

	/**
	 * @param studentId
	 * @return 해당 학생의 현재 학적 상태 (.getStatus())
	 */
	@Transactional
	public StuStat readCurrentStatus(Long studentId) {
		return stuStatRepository.findAllByStudentIdOrderByIdDesc(studentId).get(0);
	}

	/**
	 * @param studentId
	 * @return 해당 학생의 전체 학적 변동 내역 조회
	 */
	@Transactional
	public List<StuStat> readStatusList(Long studentId) {
		return stuStatRepository.findAllByStudentIdOrderByIdDesc(studentId);
	}

	/**
	 * 모든 학생 id 리스트
	 */
	public List<Long> readIdList() {
        return studentRepository.findAllStudentIds();
	}

	/*
	 * 처음 학생이 생성될 때 학적 상태 지정 (재학)
	 *
	 * 첫 학적 상태 저장과 이후 변동 사항을 저장할 때의 메서드를 분리한 이유는 이후 변동 사항을 지정할 때에는 기존의 상태 데이터의
	 * toDate를 현재 날짜로 바꿔주는 작업이 추가로 필요하기 때문임
	 */
	@Transactional
	public void createFirstStatus(Long studentId) {
		Student student = studentRepository.findById(studentId).orElseThrow(
				() -> new CustomRestfullException("학생을 조회할 수 없습니다.", HttpStatus.NOT_FOUND)
		);
		StuStat stuStat = new StuStat();
		// 기본값 세팅을 이렇게 해야할까 ..?
		stuStat.setStudent(student);
		stuStat.setStatus("재학");
		stuStat.setFromDate(LocalDate.parse("9999-01-01")); // LocalDate 에서 어떻게 넣어야 할 지 다시 생각해보기
		stuStat.setBreakApp(null);
		stuStatRepository.save(stuStat);
	}

	/**
	 * 학적 상태 변동 새로운 상태 추가 + 기존 학적 상태의 to_date를 now()로 변경 breakAppId가 없다면 null로 받기
	 * 꼭 저렇게 다 나눠서 받아야 하는건가? @param 이용해서 ..?
	 */
	public void updateStatus(Long studentId, String newStatus, String newToDate, Long breakAppId) {
		// 가장 최근의 기존 학적 상태 데이터의 id
		Long targetId = stuStatRepository.findAllByStudentIdOrderByIdDesc(studentId).get(0).getId();
		// 이렇게 찾는 게 맞는지 + 에러도 잘 구현한 건지 확인 할 것
		Student student = studentRepository.findById(studentId).orElseThrow(
				() -> new CustomRestfullException("학생을 조회할 수 없습니다.", HttpStatus.NOT_FOUND)
		);
		// breakApp 찾기
		BreakApp breakApp = breakAppRepository.findById(breakAppId).orElseThrow(
				() -> new CustomRestfullException("휴학 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
		);
		StuStat stuStat = new StuStat();
		// 기본값 세팅을 이렇게 해야할까 ..?
		// 새로운 학적 상태 추가
		stuStat.setStudent(student);
		stuStat.setStatus(newStatus);
		stuStat.setFromDate(LocalDate.parse(newToDate));
		stuStat.setToDate(LocalDate.now());
		stuStat.setBreakApp(breakApp);
		stuStatRepository.save(stuStat);
	}

}
