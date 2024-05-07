package frontEnd.semantics;

import errorHandlers.SemanticErrorHandler;
import frontEnd.semantics.symbolTable.symbol.Symbol;
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


    /**
     * Function to check the semantic of the tree received from the parser.
     * @param tree the tree that we receive from the parser.
     */
    public static void sendTree(Tree tree) {
        //TODO implement this method
    }


    /**
     * Function to check if a symbol is declared in the current scope.
     * @param symbol the symbol to check.
     */
    public void checkDeclaration(Symbol symbol) {
        // Check if the symbol can be declared in the scope
        /*
        if (symbolTable.currentScope().contains(symbol.getName())) {
            errorHandler.reportError(, symbol.getLineDeclaration(), 0, "Duplicate symbol declaration");
        } else {
            symbolTable.addSymbol(symbol);
        }*/
    }

    /**
     * Function to check if a symbol is compatible with the type of the current scope.
     * @param symbol the symbol to check.
     */
    public void checkTypeCompatibility(Symbol symbol) {
        // Check if the symbol is compatible with the type of the current scope
        /*
        if (symbolTable.currentScope().contains(symbol.getName())) {
            errorHandler.reportError(, symbol.getLineDeclaration(), 0, "Duplicate symbol declaration");
        } else {
            symbolTable.addSymbol(symbol);
        }*/
    }



    // Additional methods for semantic checks can be added here
}
