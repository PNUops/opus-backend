package com.opus.opus.global.error;

public class FileDeleteFailedException extends RuntimeException {

    public FileDeleteFailedException() {
        super();
    }

    public FileDeleteFailedException(String message) {
        super(message);
    }

    public FileDeleteFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileDeleteFailedException(Throwable cause) {
        super(cause);
    }
}
