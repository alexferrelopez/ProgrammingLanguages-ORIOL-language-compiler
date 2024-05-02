package FrontEnd;

import FrontEnd.Dictionary.Token;
import FrontEnd.Exceptions.InvalidFileException;
import FrontEnd.Exceptions.InvalidTokenException;

import java.util.*;

public class RecursiveDescentLLParser {
	private final LexicalAnalyzer lexicalAnalyzer;

	private Token lookahead;

	public RecursiveDescentLLParser(LexicalAnalyzer lexicalAnalyzer) {
		this.lexicalAnalyzer = lexicalAnalyzer;
	}

	public void startCodeAnalysis() {
		//lexicalAnalysis();
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
		Grammar g = new Grammar();
		grammar = g.getGrammar();

		ParsingTable parsingTable = new ParsingTable(grammar);

		Stack<AbstractSymbol> stack = new Stack<>();
		NonTerminalSymbol axioma = g.getAxioma();
		//Tree tree = new Tree<AbstractSymbol>(axioma); TODO Crear l'arbre
		if(Objects.isNull(axioma)){
			//TODO throw an exception
		}else{
			stack.push(new TerminalSymbol("EOF"));
			stack.push(axioma);
		}
		try {
			lexicalAnalyzer.openCodeFile();
			lookahead = lexicalAnalyzer.getNextToken();
			System.out.println("Stack: " + stack);
			while(!stack.empty()){
				AbstractSymbol symbol = stack.pop();
				if(symbol.isTerminal()){
					match((TerminalSymbol) symbol);
					if(symbol.getName().equals("EOF") && lookahead.getLexeme().equals("EOF")){
						System.out.println("ACCEPT");
					}
				}else{
					List<AbstractSymbol> output = parsingTable.getProduction((NonTerminalSymbol) symbol, lookahead);
					if(Objects.isNull(output)){
						System.out.println("Error gramatical"); //TODO throw exception
						break;
					}
					for (int i = output.size()-1; i >=0 ; i--) {
						if(!output.get(i).getName().equals(TerminalSymbol.EPSILON)){
							stack.push(output.get(i));
						}
					}
				}
				System.out.println("Stack: " + stack);
			}
		} catch (InvalidFileException | InvalidTokenException invalidFile) {
			invalidFile.printStackTrace();
		}


		for(NonTerminalSymbol nt: grammar.keySet()){
			System.out.print("\nFirsts of " + nt.getName() + " are: ");
			for(Token terminal: First.getFirstsToken(grammar, nt, lexicalAnalyzer)){
				System.out.print(terminal.getLexeme() + " ") ;
			}

            System.out.print("\nFollows of " + nt.getName() + " are: ");
			for(Token terminal: Follow.getFollowsToken(grammar, nt, lexicalAnalyzer)){
				System.out.print(terminal.getLexeme() + " ") ;
			}
		}

	}

	private void match(TerminalSymbol terminal) {

		if(terminal.getName().equals(String.valueOf(lookahead.getType()))){
			System.out.println("MATCH");
			try {
				lookahead = lexicalAnalyzer.getNextToken();
			} catch (InvalidTokenException e) {
				e.printStackTrace();
			}
		}else{
			System.out.println("ERROR NO MATCH entre " + terminal.getName() + " i " + lookahead.getType() + " :(");
		}
	}
}