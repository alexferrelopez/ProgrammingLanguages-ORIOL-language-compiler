package FrontEnd.lexic;

import FrontEnd.Exceptions.InvalidFileException;
import FrontEnd.Exceptions.InvalidTokenException;
import FrontEnd.Dictionary.Token;

public interface LexicalAnalyzerInterface {

	/**
	 * Open the code file and start the lexical analysis. If the file does not exist, throw an exception.
	 * @throws InvalidFileException If the file does not exist.
	 */
	void startLexicalAnalysis() throws InvalidFileException;

	/**
	 * Get the next token from the code file. If the file has no more tokens, return EOF.
	 * @return  The next token in the file (based on the current word).
	 * @throws InvalidTokenException    If the token is not found in any of the enums (does not exist in the language).
	 */
	Token getNextToken() throws InvalidTokenException;
}