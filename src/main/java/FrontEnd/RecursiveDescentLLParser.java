package FrontEnd;

import FrontEnd.Dictionary.Token;
import FrontEnd.Exceptions.InvalidTokenException;

public class RecursiveDescentLLParser {
	private final LexicalAnalyzer lexicalAnalyzer;

	public RecursiveDescentLLParser(LexicalAnalyzer lexicalAnalyzer) {
		this.lexicalAnalyzer = lexicalAnalyzer;
	}

	public void startCodeAnalysis() {
		lexicalAnalysis();
	}

	private void lexicalAnalysis() {
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
	}
}
