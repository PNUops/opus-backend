package com.opus.opus.modules.contest.exception;

import com.opus.opus.global.base.BaseExceptionType;
import org.springframework.http.HttpStatus;

public enum ContestCategoryExceptionType implements BaseExceptionType {

    NOT_FOUND_CATEGORY(HttpStatus.NOT_FOUND, "존재하지 않는 카테고리입니다."),
    CATEGORY_NAME_ALREADY_EXIST(HttpStatus.CONFLICT, "동일한 카테고리명이 있습니다."),
    ONLY_CUSTOM_MODE_CAN_CHANGE_CATEGORY_SORT(HttpStatus.FORBIDDEN, "CUSTOM 모드에서만 카테고리 정렬을 수정할 수 있습니다."),
    ONLY_CUSTOM_MODE_CAN_CHANGE_CONTEST_SORT(HttpStatus.FORBIDDEN, "CUSTOM 모드에서만 카테고리 내 대회 정렬을 수정할 수 있습니다."),
    DUPLICATE_CATEGORY_ID_IN_SORT_REQUEST(HttpStatus.BAD_REQUEST, "중복된 카테고리 ID가 있습니다."),
    DUPLICATE_ITEM_ORDER_IN_CATEGORY_SORT_REQUEST(HttpStatus.BAD_REQUEST, "중복된 itemOrder가 있습니다."),
    INVALID_CATEGORY_SORT_CUSTOM_REQUEST(HttpStatus.BAD_REQUEST, "저장된 카테고리 개수와 request의 카테고리 개수가 다릅니다."),
    INVALID_CATEGORY_ITEM_ORDER(HttpStatus.BAD_REQUEST, "적절하지 않은 itemOrder입니다.(최대 카테고리 개수보다 초과된 itemOrder)"),
    NOT_EXIST_CATEGORY_IN_SORT_REQUEST(HttpStatus.NOT_FOUND, "존재하지 않는 카테고리입니다."),
    DUPLICATE_CONTEST_ID_IN_CATEGORY_SORT_REQUEST(HttpStatus.BAD_REQUEST, "중복된 대회 ID가 있습니다."),
    DUPLICATE_ITEM_ORDER_IN_CATEGORY_CONTEST_SORT_REQUEST(HttpStatus.BAD_REQUEST, "중복된 itemOrder가 있습니다."),
    INVALID_CATEGORY_CONTEST_SORT_REQUEST(HttpStatus.BAD_REQUEST, "저장된 대회 개수와 request의 대회 개수가 다릅니다."),
    INVALID_CATEGORY_CONTEST_ITEM_ORDER(HttpStatus.BAD_REQUEST, "적절하지 않은 itemOrder입니다.(최대 대회 개수보다 초과된 itemOrder)"),
    NOT_EXIST_CONTEST_IN_CATEGORY(HttpStatus.NOT_FOUND, "해당 카테고리에 속한 대회가 아닙니다.");

    private final HttpStatus httpStatus;
    private final String errorMessage;

    ContestCategoryExceptionType(final HttpStatus httpStatus, final String errorMessage) {
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
