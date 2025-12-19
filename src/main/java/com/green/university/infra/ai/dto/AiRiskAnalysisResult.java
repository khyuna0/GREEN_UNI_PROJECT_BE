package com.green.university.infra.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AiRiskAnalysisResult { // AI 응답 DTO

    private String summary;          // 한 줄 요약
    private String professorGuide;   // 교수 상담 가이드
    private String studentMessage;   // 학생에게 보여줄 메시지
    private List<String> reasonTags; // ["출결", "성적", ...]

}
