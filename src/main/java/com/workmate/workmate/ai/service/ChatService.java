package com.workmate.workmate.ai.service;

import com.workmate.workmate.ai.dto.GeminiRequest;
import com.workmate.workmate.ai.dto.GeminiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class ChatService {

    private final WebClient webClient;
    private final String apiKey;
    private final String endpoint;

    public ChatService(WebClient webClient,
                       @Value("${gemini.api.key}") String apiKey,
                       @Value("${gemini.api.endpoint}") String endpoint) {
        this.webClient = webClient;
        this.apiKey = apiKey;
        this.endpoint = endpoint;
    }

    /**
     * Gemini API로 프롬프트 전송 및 응답 받기
     *
     * @param prompt 사용자 입력 프롬프트
     * @return 응답 텍스트
     */
    public Mono<String> chat(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            return Mono.error(new IllegalArgumentException("프롬프트는 비어있을 수 없습니다."));
        }

        if (apiKey == null || apiKey.isBlank()) {
            return Mono.error(new IllegalStateException("GEMINI_API_KEY 환경변수가 설정되어 있지 않습니다."));
        }

        GeminiRequest request = GeminiRequest.createRequest(prompt);

        return webClient
                .post()
                .uri(endpoint + "?key=" + apiKey)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(GeminiResponse.class)
                .map(GeminiResponse::extractText)
                .doOnError(WebClientResponseException.class, this::handleWebClientException)
                .doOnError(this::handleGeneralException);
    }

    private void handleWebClientException(WebClientResponseException ex) {
        log.error("Gemini API 호출 실패 - Status: {}, Body: {}", ex.getStatusCode(), ex.getResponseBodyAsString(), ex);
    }

    private void handleGeneralException(Throwable ex) {
        log.error("Chat 처리 중 오류 발생: {}", ex.getMessage(), ex);
    }
}
