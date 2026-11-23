package com.opus.opus.modules.invitation.exception;

import com.opus.opus.global.base.BaseExceptionType;
import org.springframework.http.HttpStatus;

public enum InvitationExceptionType implements BaseExceptionType {

    NOT_FOUND_INVITATION(HttpStatus.NOT_FOUND, "초대 정보를 찾을 수 없습니다."),
    INVALID_INVITATION_ROLE(HttpStatus.BAD_REQUEST, "외부멘토 역할만 초대할 수 있습니다."),
    ALREADY_INVITED(HttpStatus.CONFLICT, "이미 해당 대회에 초대된 이메일입니다."),
    CANCELED_INVITATION(HttpStatus.CONFLICT, "취소된 초대입니다."),
    ALREADY_ACCEPTED_INVITATION(HttpStatus.CONFLICT, "이미 수락된 초대입니다."),
    REPLACED_INVITATION(HttpStatus.CONFLICT, "새로 발급된 초대 링크가 있습니다."),
    EXPIRED_INVITATION(HttpStatus.CONFLICT, "만료된 초대 링크입니다."),
    INVITATION_EMAIL_MISMATCH(HttpStatus.FORBIDDEN, "초대받은 이메일과 로그인한 계정의 이메일이 다릅니다."),
    CANNOT_ACCEPT_INVITATION(HttpStatus.CONFLICT, "직원 계정은 초대를 수락할 수 없습니다."),
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
