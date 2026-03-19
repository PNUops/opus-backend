package com.opus.opus.global.security.oauth2;

import com.opus.opus.global.base.BaseExceptionType;
import org.springframework.http.HttpStatus;

public enum OAuthExceptionType implements BaseExceptionType {

    GOOGLE_REVOKE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "구글 연동 해제에 실패했습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String errorMessage;

    OAuthExceptionType(final HttpStatus httpStatus, final String errorMessage) {
        this.httpStatus = httpStatus;
        this.errorMessage = errorMessage;
    }

    @Override
    public HttpStatus httpStatus() {
        return httpStatus;
    }

    @Override
    public String errorMessage() {
        return errorMessage;
    }
}
