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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
// 한 개의 강의를 듣는 모든 학생을 비동기로 AI 분석 돌리는 배치 실행기
public class AiBatchService {

    private final StuSubDetailRepository stuSubDetailRepository;
    private final DropoutRiskService dropoutRiskService;
    private final SubjectAiJobRepository subjectAiJobRepository;
    private final SubjectRepository subjectRepository;


    @Async
    @Transactional // (비동기 메서드도 트랜잭션 범위 안에 들어감)
    // ☎️ 4. 비동기로 진행 (이미 3번에서 SubjectAiJob 저장됨 + DropoutRiskService에서 evaluateAndAnalyzeRisk 메서드 가져오기)
    public void runSubjectAiAsync(Long subjectId) {
        log.info("🚀 비동기 AI 분석 시작 - subjectId: {}", subjectId);

        SubjectAiJob job = subjectAiJobRepository.findBySubject_Id(subjectId)
                .orElseThrow(() -> new RuntimeException("AI Job이 없습니다."));

        // 이미 ProfessorService finalizeGrades에서 (2번) 이미 finalized를 변경해서 이렇게 가져와도 됨!
        List<StuSubDetail> details = stuSubDetailRepository.findBySubject_IdAndFinalizedTrue(subjectId);

        log.info("📋 분석 대상 학생 수: {}", details.size());

        int done = 0;
        int fail = 0;

        for (StuSubDetail detail : details) {
            StuSub stuSub = detail.getStuSub();
            try {
                dropoutRiskService.evaluateAndAnalyzeRisk(stuSub, detail);
                log.info("✅ 처리 완료 ({}/{}): {}", done+1, details.size(), stuSub.getStudent().getName());

            } catch (Exception e) {
                fail++;
                log.error("❌ AI 분석 실패 - 학생: {}", stuSub.getStudent().getName(), e);
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
        log.info("🏁 비동기 작업 종료 - 성공: {}, 실패: {}", done - fail, fail);

    }

    // ☎️ 3. Job 저장만 별도 트랜잭션으로 즉시 커밋 (ProfessorService에서 사용 할 메서드)
    //@Transactional(propagation = Propagation.REQUIRES_NEW)
    @Transactional
    public void createJob(long subjectId, int totalCount) {
        // Job 저장/갱신 (과목당 1개 유지)
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
        log.info("✅ Job 저장 완료 (아직 커밋 안 됨): jobId={}", job.getId());
        // 여기서 트랜잭션 종료(커밋)

        runSubjectAiAsync(subjectId); // ☎️ 4. @Async가 알아서 새 스레드+트랜잭션
    }


}
