package com.opus.opus.global.error;

public class FileSaveFailedException extends RuntimeException {

    public FileSaveFailedException() {
        super();
    }

    public FileSaveFailedException(String message) {
        super(message);
    }

    public FileSaveFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileSaveFailedException(Throwable cause) {
        super(cause);
    }
}
