package frontEnd.exceptions;

public class LexicException extends Exception {
	private final static String ERROR_MESSAGE = "LEXIC: Error while lexical analysis of file";

	public LexicException() {
		super(ERROR_MESSAGE);
	}

	public LexicException(String message) {
		super(message);
	}

	public LexicException(String message, Throwable cause) {
		super(message, cause);
	}
}
