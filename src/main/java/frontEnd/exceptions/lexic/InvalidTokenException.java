package frontEnd.exceptions.lexic;

import frontEnd.exceptions.LexicException;

public class InvalidTokenException extends LexicException {

    private final static String ERROR_MESSAGE = "LEXIC: Error while lexical analysis of file";

    public InvalidTokenException() {
        super(ERROR_MESSAGE);
    }

    public InvalidTokenException(String message) {
        super(message);
    }

    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
