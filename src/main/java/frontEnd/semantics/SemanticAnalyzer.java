package frontEnd.semantics;

import errorHandlers.SemanticErrorHandler;
import frontEnd.lexic.dictionary.Token;
import frontEnd.sintaxis.Tree;
import frontEnd.sintaxis.grammar.AbstractSymbol;

import java.util.ArrayList;
import java.util.List;

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

    public static void sendTree(Tree<AbstractSymbol> tree) {
        // Get a list of terminal symbols (tokens with lexical meaning).
        List<Token> tokens = new ArrayList<>();

        // Check the first node (root) to see what kind of grammatical operation is done and apply its semantics.
        switch (tree.getNode().getName()) {
            case "declaration":
                break;
            case "assignation":
                break;
            // ...
        }
    }
}
