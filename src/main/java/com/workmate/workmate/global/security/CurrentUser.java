package com.workmate.workmate.global.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {

    public Long getUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof Long)) {
            throw new RuntimeException("인증 정보가 없습니다.");
        }

        return (Long) auth.getPrincipal();
    }

    public String getUserRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || auth.getAuthorities().isEmpty()) {
            throw new RuntimeException("권한 정보가 없습니다.");
        }

        return auth.getAuthorities().iterator().next().getAuthority();
    }
}