package com.workmate.workmate;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    // GET /api/hello 호출 시 "hello" 반환
    @GetMapping("/api/hello")
    public String hello() {
        return "hello";
    }
}