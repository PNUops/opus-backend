package com.opus.opus.modules.invitation.exception;

import com.opus.opus.global.base.BaseExceptionType;
import org.springframework.http.HttpStatus;

public enum InvitationExceptionType implements BaseExceptionType {

    NOT_FOUND_INVITATION(HttpStatus.NOT_FOUND, "초대 정보를 찾을 수 없습니다."),
    INVALID_INVITATION_ROLE(HttpStatus.BAD_REQUEST, "외부멘토 역할만 초대할 수 있습니다."),
    ALREADY_INVITED(HttpStatus.CONFLICT, "이미 해당 대회에 초대된 이메일입니다."),
    ;

    private final HttpStatus httpStatus;
    private final String errorMessage;

    InvitationExceptionType(final HttpStatus httpStatus, final String errorMessage) {
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
