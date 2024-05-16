package frontEnd.semantics;

import debug.PrettyPrintTree;
import errorHandlers.SemanticErrorHandler;
import frontEnd.sintaxis.Tree;
import frontEnd.sintaxis.grammar.AbstractSymbol;

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
        printTree(tree);
        //TODO implement this method
    }

    private static void printTree(Tree<AbstractSymbol> tree) {
        PrettyPrintTree<Tree<AbstractSymbol>> printTree = new PrettyPrintTree<>(
                Tree::getChildren,
                Tree::getNode
        );

        printTree.display(tree);
    }
}
