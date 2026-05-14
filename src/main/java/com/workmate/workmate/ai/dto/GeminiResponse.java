package com.workmate.workmate.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeminiResponse {
    private List<Candidate> candidates;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Candidate {
        private Content content;
        private String finishReason;
        private Double avgLogprobs;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Content {
        private List<Part> parts;
        private String role;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Part {
        private String text;
    }

    /**
     * 응답에서 첫 번째 후보의 텍스트만 추출
     */
    public String extractText() {
        if (candidates == null || candidates.isEmpty()) {
            return "";
        }

        Candidate candidate = candidates.get(0);
        if (candidate.getContent() == null || candidate.getContent().getParts() == null
                || candidate.getContent().getParts().isEmpty()) {
            return "";
        }

        return candidate.getContent().getParts().get(0).getText();
    }
}
