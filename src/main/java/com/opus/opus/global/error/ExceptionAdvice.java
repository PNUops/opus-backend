package com.opus.opus.global.error;

import com.opus.opus.global.base.BaseException;
import com.opus.opus.modules.contest.application.dto.response.TeamBulkErrorResponse;
import com.opus.opus.modules.team.exception.TeamException;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> methodArgumentNotValidException(final MethodArgumentNotValidException e) {
        final String errorMessage = getErrorMessage(e);
        return ResponseEntity.badRequest().body(new ExceptionResponse(errorMessage));
    }

    @ExceptionHandler(TeamException.class)
    public ResponseEntity<?> teamException(final TeamException e) {
        if (e.getBulkErrors() != null) {
            return ResponseEntity.status(e.exceptionType().httpStatus())
                    .body(new TeamBulkErrorResponse(e.getBulkErrors()));
        }
        return ResponseEntity.status(e.exceptionType().httpStatus())
                .body(new ExceptionResponse(e.getMessage()));
    }

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ExceptionResponse> BaseException(final BaseException e) {
        final String errorMessage = e.getMessage();
        return ResponseEntity.status(e.exceptionType().httpStatus()).body(new ExceptionResponse(errorMessage));
    }

    private static String getErrorMessage(final BindException e) {
        final BindingResult bindingResult = e.getBindingResult();
        return bindingResult.getFieldErrors().stream()
                .map(fieldError -> {
                    assert fieldError.getRejectedValue() != null;
                    return getErrorMessage(fieldError.getField(), fieldError.getRejectedValue().toString(),
                            fieldError.getDefaultMessage());
                }).collect(Collectors.joining(", "));
    }

    private static String getErrorMessage(final String errorField, final String invalidValue,
                                          final String errorMessage) {
        return "[%s] %s : %s".formatted(errorField, invalidValue, errorMessage);
    }
}
