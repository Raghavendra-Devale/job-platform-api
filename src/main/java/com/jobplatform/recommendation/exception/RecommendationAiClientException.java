package com.jobplatform.recommendation.exception;

public class RecommendationAiClientException extends RuntimeException {
    public RecommendationAiClientException(String message) {
        super(message);
    }
    public RecommendationAiClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
