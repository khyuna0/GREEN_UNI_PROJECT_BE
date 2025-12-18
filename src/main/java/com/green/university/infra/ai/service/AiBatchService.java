package com.green.university.infra.ai.service;

import com.green.university.domain.dropoutrisk.service.DropoutRiskService;
import com.green.university.domain.subject.entity.StuSub;
import com.green.university.domain.subject.entity.StuSubDetail;
import com.green.university.domain.subject.entity.Subject;
import com.green.university.domain.subject.repository.StuSubDetailRepository;
import com.green.university.domain.subject.repository.SubjectAiJobRepository;
import com.green.university.domain.subject.repository.SubjectRepository;
import com.green.university.global.exception.CustomRestfullException;
import com.green.university.infra.ai.entity.JobStatus;
import com.green.university.infra.ai.entity.SubjectAiJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
// 한 과목 전체 학생을 비동기로 AI 분석 돌리는 배치 실행기
public class AiBatchService {

    private final StuSubDetailRepository stuSubDetailRepository;
    private final DropoutRiskService dropoutRiskService;
    private final SubjectAiJobRepository subjectAiJobRepository;
    private final SubjectRepository subjectRepository;


    @Async
    public void runSubjectAiAsync(Long subjectId) {
        SubjectAiJob job = subjectAiJobRepository.findBySubject_Id(subjectId)
                .orElseThrow(() -> new RuntimeException("AI Job이 없습니다."));

        List<StuSubDetail> details = stuSubDetailRepository.findBySubject_IdAndFinalizedTrue(subjectId);

        int done = 0;
        int fail = 0;

        for (StuSubDetail detail : details) {
            StuSub stuSub = detail.getStuSub();
            try {
                dropoutRiskService.evaluateAndAnalyzeRisk(stuSub, detail);
            } catch (Exception e) {
                fail++;
                log.warn("AI 분석 실패 - 학생({}): {}", stuSub.getStudent().getName(), e.getMessage());
            } finally {
                done++;
                job.setDoneCount(done);
                job.setMessage("AI 분석중... (" + done + "/" + details.size() + ")");
                subjectAiJobRepository.save(job);
            }
        }

        // 최종 상태
        if (fail == 0) {
            job.setStatus(JobStatus.SUCCESS);
            job.setMessage("AI 분석 완료 (" + done + "/" + details.size() + ")");
        } else {
            job.setStatus(JobStatus.FAIL);
            job.setMessage("AI 분석 일부 실패 (" + fail + "명 실패, " + done + "/" + details.size() + ")");
        }
        subjectAiJobRepository.save(job);
    }

    // 🔥 Job 저장만 별도 트랜잭션으로 즉시 커밋
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createAndStartJob(long subjectId, int totalCount) {
        // 2) Job 저장/갱신 (과목당 1개 유지)
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new CustomRestfullException("과목이 없습니다.", HttpStatus.NOT_FOUND));

        SubjectAiJob job = subjectAiJobRepository.findBySubject_Id(subjectId)
                .orElseGet(SubjectAiJob::new);

        job.setSubject(subject);
        job.setStatus(JobStatus.RUNNING);
        job.setTotalCount(totalCount);
        job.setDoneCount(0);
        job.setMessage("AI 분석 준비중...");
        subjectAiJobRepository.save(job);
        // 여기서 트랜잭션 종료(커밋)


        runSubjectAiAsync(subjectId);
    }


}
