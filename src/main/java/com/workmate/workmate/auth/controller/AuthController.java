package com.workmate.workmate.auth.controller;

import org.springframework.web.bind.annotation.*;
import com.workmate.workmate.auth.dto.LoginRequest;
import com.workmate.workmate.auth.service.AuthService;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest req) {
        String token = auth.login(req);
        return ResponseEntity.ok(token);
    }
}
