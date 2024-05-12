package frontEnd.exceptions;

public class SemanticException extends Exception {

    private final static String ERROR_MESSAGE = "LEXIC: Error while lexical analysis of file";

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
