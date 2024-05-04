package frontEnd.sintaxis;

import frontEnd.lexic.dictionary.Token;
import errorHandlers.SyntacticErrorHandler;
import frontEnd.exceptions.InvalidFileException;
import frontEnd.exceptions.InvalidTokenException;
import frontEnd.lexic.LexicalAnalyzerInterface;
import frontEnd.sintaxis.grammar.*;
import frontEnd.sintaxis.grammar.derivationRules.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;

public class RecursiveDescentLLParser implements SyntacticAnalyzerInterface {
	private final LexicalAnalyzerInterface lexicalAnalyzer;
	private final SyntacticErrorHandler errorHandler;

	private Token lookahead;

	public RecursiveDescentLLParser(LexicalAnalyzerInterface lexicalAnalyzer, SyntacticErrorHandler parserErrorHandler) {
		this.lexicalAnalyzer = lexicalAnalyzer;
		this.errorHandler = parserErrorHandler;
	}

	/**
	 * This method starts the lexical, syntactic and semantic analysis of the code.
	 */
	@Override
	public void parseProgram() {
		Grammar grammar = new Grammar();
		Map<NonTerminalSymbol, List<List<AbstractSymbol>>> grammarMap;
		grammarMap = grammar.getGrammar();//Read and load the grammar

		ParsingTable parsingTable = new ParsingTable(grammarMap);//Create and fill the parsing table with our grammar

		Stack<AbstractSymbol> stack = new Stack<>();
		NonTerminalSymbol axioma = grammar.getAxioma();
		//Tree tree = new Tree<AbstractSymbol>(axioma);
		if(Objects.isNull(axioma)){
			//TODO throw an exception
		}else{
			stack.push(new TerminalSymbol("EOF")); //Push the $ and the axioma to the stack
			stack.push(axioma);
		}
		try {
			lexicalAnalyzer.startLexicalAnalysis();
			lookahead = lexicalAnalyzer.getNextToken();
			System.out.println("Stack: " + stack);
			while(!stack.empty()){
				AbstractSymbol symbol = stack.pop();
				if(symbol.isTerminal()){ //If the symbol is a terminal we have to match it with the lookahead
					match((TerminalSymbol) symbol);
					if(symbol.getName().equals("EOF") && lookahead.getLexeme().equals("EOF")){ //if both are EOF we have finished :D
						System.out.println("ACCEPT");
					}
				}else{
					List<AbstractSymbol> output = parsingTable.getProduction((NonTerminalSymbol) symbol, lookahead); //Retrieve the predicted production
					if(Objects.isNull(output)){
						System.out.println("Error gramatical"); //TODO throw exception
						break;
					}
					for (int i = output.size()-1; i >=0 ; i--) { //Push the production to the stack unless it is epsilon
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

		//Display the firsts and follows of the grammar for debugging purposes
		First.displayAllFirsts(grammarMap);
		Follow.displayAllFollows(grammarMap);
	}



	/**
	 * This method checks if the lookahead is the same as the terminal symbol
	 * @param terminal the terminal symbol to compare
	 */
	private void match(TerminalSymbol terminal) {
		if(terminal.getName().equals(String.valueOf(lookahead.getType()))){
			System.out.println("MATCH");
			try {
				lookahead = lexicalAnalyzer.getNextToken();
			} catch (InvalidTokenException e) {
				e.printStackTrace();
			}
		}else{
			System.out.println("ERROR NO MATCH between " + terminal.getName() + " and " + lookahead.getType() + " :(");
		}
	}
}