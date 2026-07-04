package com.raghav.jobplatform.common.ai;

public class AIException extends RuntimeException {
    
    public AIException(String message) {
        super(message);
    }

    public AIException(String message, Throwable cause) {
        super(message, cause);
    }
}
