package com.workmate.workmate.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    public JwtFilter(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    /**
     * HTTP 요청에서 JWT 토큰을 추출하고 유효성을 검사하여 인증 정보를 설정하는 필터 메서드
     *
     * @param request     HTTP 요청 객체
     * @param response    HTTP 응답 객체
     * @param filterChain 필터 체인 객체
     * @throws ServletException 예외 발생 시 던지는 서블릿 예외
     * @throws IOException      예외 발생 시 던지는 입출력 예외
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        // HTTP 요청에서 JWT 토큰 추출
        String token = resolveToken(request);

        // JWT 토큰이 존재하고 유효한 경우 인증 정보 설정
        if (token != null && jwtProvider.validateToken(token)) {

            // JWT 토큰에서 사용자 ID와 권한 추출
            Long userId = jwtProvider.getUserId(token);
            String role = jwtProvider.getRole(token);

            // 추출된 사용자 ID와 권한을 기반으로 인증 객체 생성
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            List.of(new SimpleGrantedAuthority(role))
                    );
            
            // SecurityContext에 인증 정보 설정
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        // 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }

    /**
     * HTTP 요청에서 JWT 토큰을 추출하는 메서드
     *
     * @param request HTTP 요청 객체
     * @return 추출된 JWT 토큰 문자열, 존재하지 않으면 null 반환
     */
    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");

        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
