package FrontEnd;

import FrontEnd.Dictionary.Token;
import FrontEnd.Exceptions.InvalidFileException;
import FrontEnd.Exceptions.InvalidTokenException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecursiveDescentLLParser {
	private final LexicalAnalyzer lexicalAnalyzer;

	public RecursiveDescentLLParser(LexicalAnalyzer lexicalAnalyzer) {
		this.lexicalAnalyzer = lexicalAnalyzer;
	}

	public void startCodeAnalysis() {
		lexicalAnalysis();
		syntacticAnalysis();
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

	private void syntacticAnalysis(){

		Map<NonTerminalSymbol, List<List<AbstractSymbol>>> grammar = new HashMap<>();
		grammar = new Grammar().getGrammar();


		for(NonTerminalSymbol nt: grammar.keySet()){
			System.out.print("\nEls first de " + nt.getName() + " son: ");
			for(TerminalSymbol terminal: First.getFirsts(grammar, nt)){
				System.out.print(terminal.getName() + " ");
			}
			System.out.print("\nEls follows de " + nt.getName() + " son: ");
			for(TerminalSymbol terminal: Follow.getFollows(grammar, nt)){
				System.out.print(terminal.getName() + " ");
			}
			First.getFirstsToken(grammar, nt);
		}

	}
}
