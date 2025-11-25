package com.opus.opus.modules.file.exception;

import com.opus.opus.global.base.BaseExceptionType;
import org.springframework.http.HttpStatus;

public enum FileExceptionType implements BaseExceptionType {
    NOT_EXISTS_MATCHING_IMAGE_ID(HttpStatus.NOT_FOUND, "아이디와 일치하는 이미지가 없습니다"),
    NOT_EXISTS_THUMBNAIL(HttpStatus.NOT_FOUND, "팀 썸네일이 존재하지 않습니다"),
    REQUEST_NOT_OWN_IMAGE(HttpStatus.BAD_REQUEST, "자신의 것이 아닌 이미지를 요청하였습니다."),
    NOT_EXISTS_PREVIEW(HttpStatus.NOT_FOUND, "존재하지 않는 팀 프리뷰 ID 를 요청하였습니다"),
    NOT_INCLUDE_ID(HttpStatus.BAD_REQUEST, "ID는 반드시 포함되어야 합니다"),
    EXCEED_PREVIEW_LIMIT(HttpStatus.BAD_REQUEST, "프리뷰 이미지는 6장 이하입니다"),
    NOT_EXISTS_PHYSICAL_FILE(HttpStatus.NOT_FOUND, "물리적 파일이 존재하지 않습니다"),
    NOT_WEBP_CONVERTED(HttpStatus.ACCEPTED, "이미지 변환중 입니다"),
    ;

    private final HttpStatus httpStatus;
    private final String errorMessage;

    FileExceptionType(final HttpStatus httpStatus, final String errorMessage) {
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
