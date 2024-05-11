package frontEnd.exceptions;

public class InvalidAssignmentException extends Exception {

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
