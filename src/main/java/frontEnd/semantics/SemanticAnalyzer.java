package frontEnd.semantics;

import errorHandlers.SemanticErrorHandler;
import frontEnd.sintaxis.Tree;
import frontEnd.semantics.symbolTable.SymbolTableTree;

public class SemanticAnalyzer {
    private final SemanticErrorHandler errorHandler;
    private SymbolTableTree symbolTable;

    public SemanticAnalyzer(SemanticErrorHandler semanticErrorHandler, SymbolTableTree symbolTable) {
        this.errorHandler = semanticErrorHandler;
        this.symbolTable = symbolTable;
    }

    /*
	List<Token> tokens; { AbstractSymbol=tipusExpressio, VARIABLE, VALUE, ... }

	switch(tokens.get(0).getType()) {
		case FOR:
			// Do something
			break;
		case VARIABLE:
			// Do something
			break;
		case VALUE:
			// Do something
			break;
		...
	}
	 */



    public static void sendTree(Tree tree) {
        //TODO implement this method
    }
}
