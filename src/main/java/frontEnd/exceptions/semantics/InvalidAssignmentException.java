package frontEnd.exceptions.semantics;

import frontEnd.exceptions.SemanticException;

public class InvalidAssignmentException extends SemanticException {

    private final static String ERROR_MESSAGE = "LEXIC: Error while lexical analysis of file";

    public InvalidAssignmentException() {
        super(ERROR_MESSAGE);
    }

    public InvalidAssignmentException(String message) {
        super(message);
    }

    public InvalidAssignmentException(String message, Throwable cause) {
        super(message, cause);
    }
}
