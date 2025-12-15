package com.green.university.service;

import com.green.university.dto.AiRiskAnalysisRequest;
import com.green.university.dto.response.AiRiskAnalysisResult;
import com.green.university.entity.DropoutRisk;
import com.green.university.entity.RiskType;
import com.green.university.entity.Student;
import com.green.university.entity.Subject;
import com.green.university.repository.DropoutRiskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiAnalysisService {

    private final DropoutRiskRepository dropoutRiskRepository;
    private final WebClient mistralWebClient;
    private final WebClient geminiWebClient;
    private final ObjectMapper objectMapper;

    @Value("${app.ai.mistral.model}")
    private String mistralModel;

    public AiRiskAnalysisResult analyzeRisk(AiRiskAnalysisRequest request) {
        // 프롬프트 구성
        String prompt = buildPrompt(request);

        // 실제 호출용 DTO
        Map<String, Object> body = Map.of(
                "model", "your-gemini-model",
                "contents", List.of(
                        Map.of("role", "user", "parts", List.of(
                                Map.of("text", prompt)
                        ))
                )
        );

        // 여기서는 동기(block) 예시. 실무에서 비동기로 바꿀 수도 있음. [web:11]
        GeminiResponseDto response = geminiWebClient.post()
                .uri("/v1beta/models/your-gemini-model:generateContent")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(GeminiResponseDto.class)
                .block();

        // 응답 파싱 → AiRiskAnalysisResult로 변환
        return mapGeminiResponse(response);
    }

    private String buildPrompt(AiRiskAnalysisRequest req) {
        // 실제로는 StringBuilder로 정리
        return """
                너는 20년 이상의 대학교 상담 컨설턴트다.
                아래 학생의 출결, 과제, 성적 정보를 보고 중도 이탈 위험을 분석해라.
                - 출력 형식: JSON (summary, professorGuide, studentMessage, reasonTags 배열)

                학생 정보:
                - 이름: %s
                - 과목: %s
                - 결석: %d회
                - 지각: %d회
                - 환산점수: %.1f점
                - 등급: %s
                - 학기 GPA: %s
                - 위험타입: %s
                - 위험레벨: %s
                """.formatted(
                req.getStudentName(),
                req.getSubjectName(),
                req.getAbsent(),
                req.getLateness(),
                req.getConvertedMark(),
                req.getGrade(),
                req.getSemesterGpa() == null ? "N/A" : req.getSemesterGpa(),
                req.getRiskType(),
                req.getRiskLevel()
        );
    }

    private AiRiskAnalysisResult mapGeminiResponse(GeminiResponseDto response) {
        // 여기서 response 구조에 맞게 JSON 파싱
        // 실무에서는 content 안의 text를 JSON으로 다시 파싱
        // 예시로만 둠
        AiRiskAnalysisResult result = new AiRiskAnalysisResult();
        result.setSummary("...");         // 파싱 결과
        result.setProfessorGuide("...");
        result.setStudentMessage("...");
        result.setReasonTags(List.of("ATTENDANCE"));
        return result;
    }
}



    /**
    @Transactional
    public void analyzeAndSaveMerged(Long riskId) {
        DropoutRisk trigger = dropoutRiskRepository.findById(riskId)
                .orElseThrow(() -> new RuntimeException("risk not found"));

        Student student = trigger.getStudent();
        Subject subject = trigger.getSubject();

        String prompt = (subject != null)
                ? buildSubjectMergedPrompt(student, subject)
                : buildSemesterPrompt(student, trigger);

        String analysisText = callMistralAndFormat(prompt);

        trigger.setAiAnalysis(analysisText);
        dropoutRiskRepository.save(trigger);
    }

    private String buildSubjectMergedPrompt(Student student, Subject subject) {
        List<DropoutRisk> list =
                dropoutRiskRepository.findByStudent_IdAndSubject_Id(student.getId(), subject.getId());

        String attendance = list.stream()
                .filter(r -> r.getRiskType() == RiskType.ATTENDANCE)
                .map(r -> r.getRiskLevel().name())
                .findFirst().orElse("NONE");

        String grade = list.stream()
                .filter(r -> r.getRiskType() == RiskType.SUBJECT_GRADE)
                .map(r -> r.getRiskLevel().name())
                .findFirst().orElse("NONE");

        // ✅ %s 5개, 값 5개 (절대 안 꼬임)
        return String.format("""
                너는 대학교 상담 심리학자야.
                반드시 "순수 JSON 오브젝트"만 출력해. 다른 텍스트/마크다운 금지.
                아래 스키마에서 모든 값은 무조건 문자열(String)로만 작성해(배열/객체 금지).
                {"reason":"...","counseling_script":"...","student_action":"..."}

                입력:
                이름=%s
                학과=%s
                과목=%s
                출석위험=%s
                성적위험=%s
                """,
                student.getName(),
                student.getDepartment().getName(),
                subject.getName(),
                attendance,
                grade
        );
    }

    private String buildSemesterPrompt(Student student, DropoutRisk trigger) {
        // ✅ 학기 누계용: %s 4개, 값 4개 (절대 안 꼬임)
        return String.format("""
                너는 대학교 상담 심리학자야.
                반드시 "순수 JSON 오브젝트"만 출력해. 다른 텍스트/마크다운 금지.
                아래 스키마에서 모든 값은 무조건 문자열(String)로만 작성해(배열/객체 금지).
                {"reason":"...","counseling_script":"...","student_action":"..."}

                입력:
                이름=%s
                학과=%s
                위험수준=%s
                참고입력=%s
                """,
                student.getName(),
                student.getDepartment().getName(),
                String.valueOf(trigger.getRiskLevel()),
                String.valueOf(trigger.getLastAiInput())
        );
    }

    private String callMistralAndFormat(String prompt) {
        String body = String.format("""
                {
                  "model": "%s",
                  "messages": [{"role":"user","content":"%s"}],
                  "response_format": {"type":"json_object"}
                }
                """, mistralModel, escapeJson(prompt));

        String raw = mistralWebClient.post()
                .uri("/v1/chat/completions")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return parseAndFormat(raw);
    }

    private String parseAndFormat(String rawResponse) {
        try {
            JsonNode root = objectMapper.readTree(rawResponse);

            JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
            String content = contentNode.isMissingNode() || contentNode.isNull() ? null : contentNode.asText();

            if (content == null || content.isBlank()) {
                return "[AI 파싱 실패] content 없음";
            }

            // content는 JSON 문자열이므로 한 번 더 파싱
            JsonNode json = objectMapper.readTree(content);

            String reason = nodeToSafeString(json.get("reason"));
            String script = nodeToSafeString(json.get("counseling_script"));
            String action = nodeToSafeString(json.get("student_action"));

            return String.format("[원인] %s\n[상담 멘트] %s\n[학생 액션] %s", reason, script, action);

        } catch (Exception e) {
            String preview = (rawResponse == null) ? "null"
                    : rawResponse.substring(0, Math.min(rawResponse.length(), 400));
            log.error("Mistral 파싱 실패 rawPreview={}", preview, e);
            return "[AI 파싱 실패] 응답 형식이 예상과 다름 (로그 확인)";
        }
    }

    // ✅ JsonNode가 문자열/배열/객체 뭐로 와도 절대 안 터지게 "문자열"로 만들어줌
    private String nodeToSafeString(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) return "";

        if (node.isTextual()) return node.asText();

        // 배열/객체면 JSON 문자열로 저장(가장 안전)
        try {
            return objectMapper.writeValueAsString(node);
        } catch (Exception e) {
            return node.asText();
        }
    }

    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "")
                .replace("\t", "\\t");
    }
}
*/