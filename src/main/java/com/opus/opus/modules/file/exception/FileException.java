package com.opus.opus.modules.file.exception;

import com.opus.opus.global.base.BaseException;
import com.opus.opus.global.base.BaseExceptionType;

public class FileException extends BaseException {

    private final FileExceptionType exceptionType;

    public FileException(final FileExceptionType exceptionType) {
        super(exceptionType.errorMessage());
        this.exceptionType = exceptionType;
    }

    public FileException(final FileExceptionType exceptionType, final String message) {
        super(message);
        this.exceptionType = exceptionType;
    }

    @Override
    public BaseExceptionType exceptionType() {
        return exceptionType;
    }
}
