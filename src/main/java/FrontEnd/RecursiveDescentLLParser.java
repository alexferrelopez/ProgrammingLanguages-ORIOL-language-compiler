package FrontEnd;

import FrontEnd.Dictionary.Token;
import ErrorHandlers.AbstractErrorHandler;
import ErrorHandlers.ErrorTypes.ParserErrorType;
import ErrorHandlers.ParserErrorHandler;
import ErrorHandlers.WarningTypes.ParserWarningType;
import FrontEnd.Exceptions.InvalidFileException;
import FrontEnd.Exceptions.InvalidTokenException;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;

public class RecursiveDescentLLParser {
	private final LexicalAnalyzer lexicalAnalyzer;
	private final ParserErrorHandler errorHandler;

	private Token lookahead;

	public RecursiveDescentLLParser(LexicalAnalyzer lexicalAnalyzer, AbstractErrorHandler<ParserErrorType, ParserWarningType> parserErrorHandler) {
		this.lexicalAnalyzer = lexicalAnalyzer;
		this.errorHandler = (ParserErrorHandler) parserErrorHandler;
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
                token = lexicalAnalyzer.getNextToken();

            } while (!token.isEOF());
		} catch (InvalidFileException e) {
			System.out.println(e.getMessage());
		} catch (InvalidTokenException e) {
            throw new RuntimeException(e);
        }
    }

	private void syntacticAnalysis(){

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
			lexicalAnalyzer.openCodeFile();
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
		First.displayAllFirsts(grammarMap, lexicalAnalyzer);
		Follow.displayAllFollows(grammarMap, lexicalAnalyzer);
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