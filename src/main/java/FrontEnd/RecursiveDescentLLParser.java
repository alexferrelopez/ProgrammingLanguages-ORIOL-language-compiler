package FrontEnd;

import FrontEnd.Dictionary.Token;
import FrontEnd.ErrorHandlers.AbstractErrorHandler;
import FrontEnd.ErrorHandlers.ErrorTypes.ParserErrorType;
import FrontEnd.ErrorHandlers.ParserErrorHandler;
import FrontEnd.ErrorHandlers.WarningTypes.ParserWarningType;
import FrontEnd.Exceptions.InvalidFileException;
import FrontEnd.Exceptions.InvalidTokenException;

public class RecursiveDescentLLParser {
	private final LexicalAnalyzer lexicalAnalyzer;
	private final ParserErrorHandler errorHandler;


	public RecursiveDescentLLParser(LexicalAnalyzer lexicalAnalyzer, AbstractErrorHandler<ParserErrorType, ParserWarningType> parserErrorHandler) {
		this.lexicalAnalyzer = lexicalAnalyzer;
		this.errorHandler = (ParserErrorHandler) parserErrorHandler;
	}

	public void startCodeAnalysis() {
		lexicalAnalysis();
	}

	private void lexicalAnalysis() {
		try {
			lexicalAnalyzer.openCodeFile();

			// Get all the tokens from the grammar.
			Token token;
			do {
				try {
					token = lexicalAnalyzer.getNextToken();
				} catch (InvalidTokenException e) {
					// Do something
					System.out.println(e.getMessage());
					break;
				}
			} while (!token.isEOF());
		} catch (InvalidFileException e) {
			System.out.println(e.getMessage());
		}
	}
}
