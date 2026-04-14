package com.workmate.workmate.auth.service;

import org.springframework.stereotype.Service;
import com.workmate.workmate.auth.dto.LoginRequest;
import com.workmate.workmate.global.security.JwtProvider;
import com.workmate.workmate.user.repository.UserRepository;
import com.workmate.workmate.user.entity.User;
import com.workmate.workmate.auth.dto.SignupRequest;
import com.workmate.workmate.auth.dto.SignupResponse;
import com.workmate.workmate.user.entity.Role;

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

    /**
     * 사용자 회원가입 메서드. 새로운 사용자를 등록하고 회원가입 성공 메시지와 함께 사용자 정보를 반환한다.
     * @param req 회원가입 요청 객체로, 이메일, 비밀번호, 이름, 역할을 포함한다.
     * @return 회원가입이 성공하면 응답 메시지와 함께 사용자 이메일과 이름을 포함하는 SignupResponse 객체를 반환한다.
     */
    public SignupResponse signup(SignupRequest req) {
        // 회원가입 로직 구현
        // 예시: 사용자 저장 후 응답 객체 생성
        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }

        if (req.getPassword().length() < 6) {
            throw new RuntimeException("비밀번호는 최소 6자 이상이어야 합니다.");
        }

        if (req.getName() == null || req.getName().isEmpty()) {
            throw new RuntimeException("이름은 필수 입력 사항입니다.");
        }

        if (!req.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new RuntimeException("유효한 이메일 형식이 아닙니다.");
        }

        if (req.getRole() != 0 && req.getRole() != 1) {
            throw new RuntimeException("유효한 역할이 아닙니다. 0: WORKER, 1: OWNER");
        }

        Role role = (req.getRole() == 0) ? Role.WORKER : Role.ADMIN;


        User newUser = new User();
        newUser.setEmail(req.getEmail());
        newUser.setPassword(req.getPassword());
        newUser.setName(req.getName());
        newUser.setRole(role);
        userRepository.save(newUser);

        return new SignupResponse("회원가입 성공", newUser.getEmail(), newUser.getName(), newUser.getRole());
    }
}
