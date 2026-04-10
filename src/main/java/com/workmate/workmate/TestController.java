package com.workmate.workmate;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "테스트", description = "테스트용 API")
public class TestController {

    @Operation(summary = "Hello 메시지 반환", description = "간단한 인사 메시지를 반환합니다.")
    @ApiResponse(responseCode = "200", description = "성공적으로 메시지 반환")
    @GetMapping("/api/hello")
    public String hello() {
        return "hello";
    }
}