package com.opus.opus.modules.contest.exception;

import com.opus.opus.global.base.BaseExceptionType;
import org.springframework.http.HttpStatus;

public enum ContestTeamTemplateExceptionType implements BaseExceptionType {
    NOT_FOUND_TEMPLATE(HttpStatus.NOT_FOUND, "템플릿을 찾을 수 없습니다."),
    INVALID_TEMPLATE_FIELD_TYPE(HttpStatus.BAD_REQUEST, "TemplateFieldType은 REQUIRED, OPTIONAL, HIDDEN 중 하나입니다.");

    private final HttpStatus httpStatus;
    private final String errorMessage;

    ContestTeamTemplateExceptionType(final HttpStatus httpStatus, final String errorMessage) {
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
