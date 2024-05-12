package frontEnd.exceptions;

public class SemanticException extends Exception {

    private final static String ERROR_MESSAGE = "SEMANTIC: Error while semantic analysis of code.";

    public SemanticException() {
        super(ERROR_MESSAGE);
    }

    public SemanticException(String message) {
        super(message);
    }

    public SemanticException(String message, Throwable cause) {
        super(message, cause);
    }
}
