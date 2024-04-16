package FrontEnd.Exceptions;

public class InvalidFileException extends Exception {

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
