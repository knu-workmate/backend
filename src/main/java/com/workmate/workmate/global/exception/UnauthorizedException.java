package com.workmate.workmate.global.exception;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "권한 부족 예외")
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
