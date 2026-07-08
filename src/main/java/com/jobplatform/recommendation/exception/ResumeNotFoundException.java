package com.jobplatform.recommendation.exception;

public class ResumeNotFoundException extends RuntimeException {
    public ResumeNotFoundException(String message) {
        super(message);
    }
    public ResumeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
