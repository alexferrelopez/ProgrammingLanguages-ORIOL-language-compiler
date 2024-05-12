package frontEnd.exceptions.lexic;

import frontEnd.exceptions.LexicException;

public class InvalidFileException extends LexicException {

    private final static String ERROR_MESSAGE = "LEXIC: Error while lexical analysis of file";

    public InvalidFileException() {
        super(ERROR_MESSAGE);
    }

    public InvalidFileException(String message) {
        super(message);
    }

    public InvalidFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
