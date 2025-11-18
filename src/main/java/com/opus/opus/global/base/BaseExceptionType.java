package com.opus.opus.global.base;

import org.springframework.http.HttpStatus;

public interface BaseExceptionType {

    HttpStatus httpStatus();

    String errorMessage();
}

