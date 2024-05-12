package frontEnd.exceptions.semantics;

import frontEnd.exceptions.SemanticException;

public class InvalidValueTypeException extends SemanticException {

    private final static String ERROR_MESSAGE = "LEXIC: Error while lexical analysis of file";

    public InvalidValueTypeException() {
        super(ERROR_MESSAGE);
    }

    public InvalidValueTypeException(String message) {
        super(message);
    }

    public InvalidValueTypeException(String message, Throwable cause) {
        super(message, cause);
    }
}
