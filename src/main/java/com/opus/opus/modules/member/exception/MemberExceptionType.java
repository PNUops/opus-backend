package com.opus.opus.modules.member.exception;

import com.opus.opus.global.base.BaseExceptionType;
import org.springframework.http.HttpStatus;

public enum MemberExceptionType implements BaseExceptionType {

    NOT_FOUND_MEMBER(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."),
    NOT_VERIFIED_EMAIL_AUTH(HttpStatus.BAD_REQUEST, "이메일 인증이 완료되지 않았습니다."),
    NOT_PUSAN_UNIVERSITY_EMAIL(HttpStatus.BAD_REQUEST, "부산대 이메일만 가입 가능합니다."),
    ALREADY_EXIST_EMAIL(HttpStatus.BAD_REQUEST, "이미 존재하는 이메일입니다."),
    ALREADY_EXIST_STUDENT_ID(HttpStatus.BAD_REQUEST, "이미 존재하는 학번입니다."),
    CANNOT_MATCH_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호가 틀렸습니다."),
    NOT_ADMIN(HttpStatus.FORBIDDEN, "관리자 권한이 없습니다."),
    CANNOT_CHANGE_SAME_PASSWORD(HttpStatus.BAD_REQUEST, "동일한 비밀번호로 변경할 수 없습니다."),
    EMAIL_AUTH_CODE_MISMATCH(HttpStatus.BAD_REQUEST, "이메일 인증 코드가 일치하지 않습니다."),
    EMAIL_AUTH_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "이메일 인증 코드 만료 시간이 초과되었습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String errorMessage;

    MemberExceptionType(final HttpStatus httpStatus, final String errorMessage) {
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
