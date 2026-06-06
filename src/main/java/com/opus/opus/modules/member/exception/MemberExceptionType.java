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
    CANNOT_MATCH_EMAIL_AUTH_CODE(HttpStatus.BAD_REQUEST, "이메일 인증 코드가 일치하지 않습니다."),
    CANNOT_VERIFY_EXPIRED_EMAIL_AUTH_CODE(HttpStatus.BAD_REQUEST, "이메일 인증 코드 만료 시간이 초과되었습니다."),
    MISMATCH_STUDENT_ID_AND_NAME(HttpStatus.BAD_REQUEST, "입력한 학번과 이름이 일치하지 않습니다."),
    NOT_FOUND_STAFF_INFO(HttpStatus.BAD_REQUEST, "일치하는 교직원 정보가 없습니다."),
    EMAIL_AUTH_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "24시간 내 요청 횟수를 초과했습니다. 24시간 후 다시 시도해주세요."),
    SOCIAL_MEMBER_CANNOT_USE_GENERAL_LOGIN(HttpStatus.BAD_REQUEST, "소셜 로그인 회원은 일반 로그인을 사용할 수 없습니다."),
    GENERAL_MEMBER_CANNOT_USE_SOCIAL_LOGIN(HttpStatus.BAD_REQUEST, "일반 회원으로 가입된 이메일입니다. 일반 로그인을 이용해주세요."),
    SOCIAL_TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "다른 소셜 로그인으로 가입된 이메일입니다."),
    CANNOT_UPDATE_STUDENT_ID(HttpStatus.BAD_REQUEST, "소셜 회원 가입자만 1회의 학번 수정이 가능합니다."),
    INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "startDate와 endDate를 모두 입력해주세요"),
    INVALID_DATE_ORDER(HttpStatus.BAD_REQUEST, "startDate는 endDate보다 이전이어야 합니다."),
    INVALID_SORT_VALUE(HttpStatus.BAD_REQUEST, "정렬 기준은 latest 또는 oldest만 가능합니다."),
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
