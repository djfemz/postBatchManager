package com.aspacelife.postbatch.exception;

public class BatchInsertException extends RuntimeException {

    public BatchInsertException(String message) {
        super(message);
    }

    public BatchInsertException(String message, Throwable cause) {
        super(message, cause);
    }
}
