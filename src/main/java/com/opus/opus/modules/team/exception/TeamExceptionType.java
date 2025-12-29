package com.opus.opus.modules.team.exception;

import com.opus.opus.global.base.BaseExceptionType;
import org.springframework.http.HttpStatus;

public enum TeamExceptionType implements BaseExceptionType {

    NOT_FOUND_TEAM(HttpStatus.NOT_FOUND, "팀이 존재하지 않습니다."),
    CONTEST_HAS_TEAM(HttpStatus.CONFLICT, "해당 대회에 속한 팀이 존재합니다."),
    TRACK_HAS_TEAM(HttpStatus.CONFLICT, "해당 분과에 속한 팀이 존재합니다."),

    FILE_REQUIRED(HttpStatus.BAD_REQUEST, "파일은 필수입니다."),
    INVALID_FILE_EXTENSION(HttpStatus.BAD_REQUEST, ".xlsx 파일만 업로드 가능합니다."),
    INVALID_SHEET_NAME(HttpStatus.BAD_REQUEST, "'팀 등록' 시트를 찾을 수 없습니다."),
    EMPTY_EXCEL_FILE(HttpStatus.BAD_REQUEST, "등록할 팀 데이터가 없습니다."),
    FILE_SIZE_EXCEEDED(HttpStatus.PAYLOAD_TOO_LARGE, "파일 크기는 10MB를 초과할 수 없습니다."),
    EXCEL_PARSE_ERROR(HttpStatus.BAD_REQUEST, "Excel 파일 파싱 중 오류가 발생했습니다."),

    MEMBER_COUNT_MISMATCH(HttpStatus.BAD_REQUEST, "%d번째 행: 팀원 이름과 학번의 개수가 일치하지 않습니다."),
    TEAM_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "%d번째 행: 팀 이름은 필수입니다."),
    PROJECT_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "%d번째 행: 프로젝트 이름은 필수입니다."),
    LEADER_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "%d번째 행: 팀장 이름은 필수입니다."),
    LEADER_STUDENT_ID_REQUIRED(HttpStatus.BAD_REQUEST, "%d번째 행: 팀장 학번은 필수입니다.")
    ;

    private final HttpStatus httpStatus;
    private final String errorMessage;

    TeamExceptionType(final HttpStatus httpStatus, final String errorMessage) {
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
