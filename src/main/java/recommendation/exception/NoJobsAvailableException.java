package recommendation.exception;

public class NoJobsAvailableException extends RuntimeException {
    public NoJobsAvailableException(String message) {
        super(message);
    }

    public NoJobsAvailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
