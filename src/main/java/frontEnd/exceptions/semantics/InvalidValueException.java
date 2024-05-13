package frontEnd.exceptions.semantics;

import frontEnd.exceptions.SemanticException;

public class InvalidValueException extends SemanticException {

    private final static String ERROR_MESSAGE = "LEXIC: Error while lexical analysis of file";

    public InvalidValueException() {
        super(ERROR_MESSAGE);
    }

    public InvalidValueException(String message) {
        super(message);
    }

    public InvalidValueException(String message, Throwable cause) {
        super(message, cause);
    }
}
