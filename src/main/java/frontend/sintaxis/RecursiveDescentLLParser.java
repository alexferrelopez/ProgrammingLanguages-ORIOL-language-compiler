package frontend.sintaxis;

import frontend.lexic.LexicalAnalyzerInterface;
import frontend.lexic.dictionary.Token;
import frontend.exceptions.InvalidFileException;
import frontend.exceptions.InvalidTokenException;

public class RecursiveDescentLLParser implements SyntacticAnalyzerInterface {
	private final LexicalAnalyzerInterface lexicalAnalyzer;

	public RecursiveDescentLLParser(LexicalAnalyzerInterface lexicalAnalyzer) {
		this.lexicalAnalyzer = lexicalAnalyzer;
	}

	/**
	 * This method starts the lexical, syntactic and semantic analysis of the code.
	 */
	@Override
	public void parseProgram() {
		try {
			lexicalAnalyzer.startLexicalAnalysis();

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
