package com.green.university.infra.ai;

import com.green.university.infra.ai.dto.AiRiskAnalysisRequest;
import com.green.university.infra.ai.dto.response.AiRiskAnalysisResult;
import com.green.university.infra.ai.dto.response.GeminiResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AiAnalysisService {

    private final WebClient mistralWebClient;
    private final WebClient geminiWebClient;
    private final ObjectMapper objectMapper; // JSON 파싱용 (Spring에 기본 내장됨)

    public AiAnalysisService(@Qualifier("geminiWebClient") WebClient geminiWebClient,
                             @Qualifier("mistralWebClient") WebClient mistralWebClient,
                             ObjectMapper objectMapper) {
        this.geminiWebClient = geminiWebClient;
        this.mistralWebClient = mistralWebClient;
        this.objectMapper = objectMapper;
    }


    public AiRiskAnalysisResult analyzeRisk(AiRiskAnalysisRequest request) {
        // 1. 프롬프트 생성
        String prompt = buildPrompt(request);

        // 2. Gemini 요청 바디 생성 (Map으로 간단히 구조화)
        // Gemini API 스펙: { "contents": [{ "parts": [{ "text": "프롬프트..." }] }] }
//        Map<String, Object> requestBody = Map.of(
//                "contents", List.of(
//                        Map.of("parts", List.of(
//                                Map.of("text", prompt)
//                        ))
//                )
//        );
        // 1. Gemini 시도
        try {
            return callGemini(prompt);
        } catch (Exception e) {
            log.warn("⚠ Gemini 호출 실패 (사유: {}). Mistral로 전환합니다.", e.getMessage());

            // 2. Gemini 실패 시 Mistral 시도 (Failover)
            try {
                return callMistral(prompt);
            } catch (Exception ex) {
                log.error("❌ 모든 AI 분석(Gemini, Mistral) 실패: ", ex);
                return new AiRiskAnalysisResult(
                        "AI 분석 실패",
                        "시스템 과부하로 분석 불가",
                        "잠시 후 다시 시도해주세요.",
                        Collections.emptyList()
                );
            }
        }
    }

    // --- [1] Gemini 호출 로직 분리 ---
    private AiRiskAnalysisResult callGemini(String prompt) {
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt))))
        );

        GeminiResponseDto response = geminiWebClient.post()
                .uri("/models/gemini-2.0-flash:generateContent")
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
                    if (clientResponse.statusCode().value() == 429) {
                        return clientResponse.createException().flatMap(Mono::error);
                    }
                    return clientResponse.bodyToMono(String.class)
                            .map(body -> new RuntimeException("Gemini 에러: " + body));
                })
                .bodyToMono(GeminiResponseDto.class)
                .retryWhen(Retry.backoff(2, Duration.ofSeconds(2)) // 2번만 짧게 재시도
                        .filter(t -> t instanceof WebClientResponseException.TooManyRequests))
                .block();

        if (response == null || response.getCandidates() == null || response.getCandidates().isEmpty()) {
            throw new RuntimeException("Gemini 응답 없음");
        }
        String text = response.getCandidates().get(0).getContent().getParts().get(0).getText();
        return parseJson(text);
    }

    // --- [2] Mistral 호출 로직 추가 ---
    private AiRiskAnalysisResult callMistral(String prompt) {
        // Mistral API 포맷: { "model": "mistral-tiny", "messages": [...] }
        Map<String, Object> requestBody = Map.of(
                "model", "mistral-small-latest", // 또는 mistral-tiny
                "messages", List.of(
                        Map.of("role", "user", "content", prompt + "\n\nJSON으로만 답변해.")
                ),
                "response_format", Map.of("type", "json_object") // JSON 강제 모드 (지원 모델인 경우)
        );

        String responseJson = mistralWebClient.post()
                .uri("/chat/completions") // 엔드포인트 주의
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class) // Mistral은 DTO 따로 안 만들고 바로 JsonNode로 깔게
                .block();

        try {
            // Mistral 응답 구조: choices[0].message.content
            JsonNode root = objectMapper.readTree(responseJson);
            String content = root.path("choices").get(0).path("message").path("content").asText();
            return parseJson(content);
        } catch (Exception e) {
            throw new RuntimeException("Mistral 파싱 실패", e);
        }
    }

//        try {
//            // 3. API 호출
//            GeminiResponseDto response = geminiWebClient.post()
//                    .uri("/models/gemini-2.0-flash:generateContent")
//                    .bodyValue(requestBody)
//                    .retrieve()
//                    .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
//                        // 429 에러면 에러 던져서 retry 유도, 나머지는 그대로 로그 찍고 에러
//                        if (clientResponse.statusCode().value() == 429) {
//                            return clientResponse.createException().flatMap(Mono::error);
//                        }
//                        return clientResponse.bodyToMono(String.class)
//                                .map(body -> {
//                                    log.error("Gemini 4xx 응답 바디: {}", body);
//                                    return new RuntimeException("Gemini 4xx 에러: " + body);
//                                });
//                    })
//                    .bodyToMono(GeminiResponseDto.class)
//                    // ★ 재시도 로직 추가 (3번 시도, 2초 간격)
//                    .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(2))
//                            .filter(throwable -> throwable instanceof WebClientResponseException.TooManyRequests))
//                    .block(); // 동기 처리 (결과 나올 때까지 대기)
//
//            // 4. 응답 파싱 (껍데기 벗기고 알맹이 JSON을 객체로 변환)
//            return parseGeminiResponse(response);
//
//        } catch (Exception e) {
//            log.warn("⚠ AI 분석 실패 (구글 서버 과부하 또는 할당량 초과): {}", e.getMessage());
//            // 사용자에게는 '분석 대기 중' 같은 예쁜 말로 리턴
//            return new AiRiskAnalysisResult(
//                    "AI 분석 대기",
//                    "현재 사용량이 많아 분석이 지연되고 있습니다.",
//                    "잠시 후 다시 시도해주세요.",
//                    Collections.emptyList()
//            );
//            // 에러 발생 시 빈 객체나 기본값 리턴 (서버가 죽지 않게)
//            //return new AiRiskAnalysisResult("AI 분석 실패", "상담 요망", "시스템 오류로 분석 불가", Collections.emptyList());
//        }
//    }

    // --- [공통] 프롬프트 & 파싱 ---
    private String buildPrompt(AiRiskAnalysisRequest req) {
        return "당신은 20년차 대학 학사 상담 전문가입니다. 아래 학생 데이터를 분석해 중도 이탈 위험을 진단하고 JSON 포맷으로 응답하세요.\n" +
                "불필요한 서론이나 마크다운(```)은 삭제하세요.\n" +
                "[학생 정보]\n" +
                "- 이름: " + req.getStudentName() + "\n" +
                "- 과목: " + req.getSubjectName() + "\n" +
                "- 결석/지각: " + req.getAbsent() + "회 / " + req.getLateness() + "회\n" +
                "- 점수/등급: " + req.getConvertedMark() + "점 / " + req.getGrade() + "\n\n" +
                "[JSON 필수 키]\n" +
                "{ \"summary\": \"한줄요약\", \"professorGuide\": \"교수 가이드\", \"studentMessage\": \"학생 메시지\", \"reasonTags\": [\"태그1\"] }";
    }

    private AiRiskAnalysisResult parseJson(String rawText) {
        String jsonString = stripToJson(rawText);
        try {
            return objectMapper.readValue(jsonString, AiRiskAnalysisResult.class);
        } catch (Exception e) {
            log.error("JSON 파싱 에러. 원문: {}", rawText);
            throw new RuntimeException("JSON 형식 오류");
        }
    }



//    // ★ 프롬프트 생성 메서드
//    private String buildPrompt(AiRiskAnalysisRequest req) {
//        StringBuilder sb = new StringBuilder();
//
//        sb.append("당신은 20년 경력의 대학 학사 경고 전문 상담 컨설턴트입니다.\n");
//        sb.append("다음 학생 데이터를 분석하여 중도 이탈(자퇴/제적) 위험성을 진단하고 JSON 포맷으로 응답하세요.\n");
//        sb.append("불필요한 서론이나 마크다운(```)은 삭제하세요.\n");
//        sb.append("[학생 데이터]\n");
//        sb.append("- 학생명: ").append(req.getStudentName()).append("\n");
//        sb.append("- 과목명: ").append(req.getSubjectName()).append("\n");
//        sb.append("- 결석: ").append(req.getAbsent()).append("회, 지각: ").append(req.getLateness()).append("회\n");
//        sb.append("- 현재 환산점수: ").append(req.getConvertedMark()).append("점, 등급: ").append(req.getGrade()).append("\n");
//        sb.append("- 직전 학기 GPA: ").append(req.getSemesterGpa() != null ? req.getSemesterGpa() : "정보없음").append("\n");
//        sb.append("- 시스템 감지 위험 유형: ").append(req.getRiskType()).append("\n");
//        sb.append("- 시스템 감지 위험 레벨: ").append(req.getRiskLevel()).append("\n\n");
//
//        sb.append("[응답 요구사항 (JSON 키 정의)]\n");
//        sb.append("1. summary: 학생 상태에 대한 날카로운 1줄 핵심 요약\n");
//        sb.append("2. professorGuide: 교수가 학생과 상담할 때 던져야 할 구체적인 질문과 조언 (경어체 사용)\n");
//        sb.append("3. studentMessage: 학생에게 시스템 알림으로 보낼 따뜻하지만 경각심을 주는 격려 메시지\n");
//        sb.append("4. reasonTags: 위험 원인 태그 배열 (예: ['잦은결석', '학업성취도저하', '중도포기위험'])\n");
//
//        return sb.toString();
//    }

//    // ★ 파싱 메서드 (실무형)
//    private AiRiskAnalysisResult parseGeminiResponse(GeminiResponseDto response) {
//        if (response == null || response.getCandidates() == null || response.getCandidates().isEmpty()) {
//            throw new RuntimeException("Gemini 응답이 비어있습니다.");
//        }
//
//        // 1. Gemini가 뱉은 텍스트 추출
//        String rawText = response.getCandidates().get(0).getContent().getParts().get(0).getText();
//
//        // 2. 마크다운 제거 (AI가 가끔 ```json ... ```
//        //String jsonString = rawText.replaceAll("```json", "").replaceAll("```");
//        String jsonString = stripToJson(rawText);
//
//        try {
//            // 3. String -> Java Object 변환 (Jackson ObjectMapper 사용)
//            return objectMapper.readValue(jsonString, AiRiskAnalysisResult.class);
//        } catch (Exception e) {
//            log.error("JSON 파싱 실패. 원문: {}", rawText);
//            throw new RuntimeException("AI 응답 형식이 올바르지 않습니다.");
//        }
//    }

    // mailbuddy에서 가져옴
    // AI가 보낸 텍스트가 코드펜스나 설명과 섞여 있어도 순수 JSON 부분만 추출
    private static String stripToJson(String s) {
        String t = s == null ? "" : s.trim();
        //if (t.isEmpty()) return t;
        if (t.startsWith("```")) {
            int first = t.indexOf('{');
            int last = t.lastIndexOf('}');
            if (first >= 0 && last > first) return t.substring(first, last + 1);
        }
        // 혹시 앞뒤로 잡다한 텍스트가 있을 경우 대비
        int first = t.indexOf('{');
        int last = t.lastIndexOf('}');
        if (first >= 0 && last > first) return t.substring(first, last + 1);
        return t;
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