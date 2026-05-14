package com.workmate.workmate.ai.controller;

import com.workmate.workmate.ai.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * Gemini API를 사용한 채팅 API
     *
     * @param query 사용자 입력 쿼리
     * @return 응답 텍스트
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, Object>> chat(@RequestParam(name = "q") String query) {
        log.info("Chat 요청 - Query: {}", query);

        return chatService.chat(query)
                .map(response -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", true);
                    result.put("query", query);
                    result.put("response", response);
                    return result;
                })
                .onErrorResume(ex -> {
                    log.error("Chat 처리 중 오류: {}", ex.getMessage(), ex);
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("success", false);
                    errorResult.put("query", query);
                    errorResult.put("error", ex.getMessage());
                    return Mono.just(errorResult);
                });
    }
}
