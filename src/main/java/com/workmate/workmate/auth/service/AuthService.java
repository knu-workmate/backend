package com.workmate.workmate.auth.service;

import org.springframework.stereotype.Service;
import com.workmate.workmate.auth.dto.LoginRequest;
import com.workmate.workmate.global.security.JwtProvider;
import com.workmate.workmate.user.repository.UserRepository;
import com.workmate.workmate.user.entity.User;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    public AuthService(UserRepository userRepository, JwtProvider jwtProvider) {
        this.userRepository = userRepository;
        this.jwtProvider = jwtProvider;
    }

    /**
     * 사용자 로그인 메서드. 이메일과 비밀번호를 검증하여 JWT 토큰을 생성하여 반환한다.
     * @param req 로그인 요청 객체로, 이메일과 비밀번호를 포함한다.
     * @return 인증이 성공하면 JWT 토큰 문자열을 반환한다.
     */
    public String login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 비밀번호 검증 (예시)
        if (!user.getPassword().equals(req.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        return jwtProvider.createToken(
                String.valueOf(user.getId()),
                user.getRole().name()
        );
    }
}
