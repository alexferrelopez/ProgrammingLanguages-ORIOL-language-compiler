package backEnd.exceptions.targetCode;

import backEnd.exceptions.TargetCodeException;

public class FailedFileCreationException extends TargetCodeException {

    private final static String ERROR_MESSAGE = "TARGET CODE: Error while creating target code file.";

    public FailedFileCreationException() {
        super(ERROR_MESSAGE);
    }

    public FailedFileCreationException(String message) {
        super(message);
    }

    public FailedFileCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
