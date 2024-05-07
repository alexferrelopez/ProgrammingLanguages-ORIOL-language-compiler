package frontEnd.semantics;

import errorHandlers.SemanticErrorHandler;
import frontEnd.Tree;

public class SemanticAnalyzer {
    private final SemanticErrorHandler errorHandler;

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

    public SemanticAnalyzer(SemanticErrorHandler semanticErrorHandler) {
        errorHandler = semanticErrorHandler;
    }

    public static void sendTree(Tree tree) {
        //TODO implement this method
    }
}
