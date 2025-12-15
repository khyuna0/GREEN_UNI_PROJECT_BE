package com.green.university.infra.ai;

import com.green.university.domain.subject.entity.StuSub;
import com.green.university.domain.subject.entity.StuSubDetail;
import com.green.university.domain.subject.repository.StuSubDetailRepository;
import com.green.university.domain.subject.repository.SubjectAiJobRepository;
import com.green.university.infra.ai.entity.JobStatus;
import com.green.university.infra.ai.entity.SubjectAiJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiBatchService {

    private final StuSubDetailRepository stuSubDetailRepository;
    private final DropoutRiskService dropoutRiskService;
    private final SubjectAiJobRepository subjectAiJobRepository;


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
}
