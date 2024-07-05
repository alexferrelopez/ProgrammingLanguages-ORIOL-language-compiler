package backEnd.exceptions;

public class TargetCodeException extends Exception {
    private final static String ERROR_MESSAGE = "TARGET CODE: Error while trying to generate target code.";

    public TargetCodeException() {
        super(ERROR_MESSAGE);
    }

    public TargetCodeException(String message) {
        super(message);
    }

    public TargetCodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
