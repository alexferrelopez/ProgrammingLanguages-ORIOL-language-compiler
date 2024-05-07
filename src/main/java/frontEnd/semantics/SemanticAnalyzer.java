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

    /**
     * Function to check the semantic of the tree received from the parser.
     * @param tree the tree that we receive from the parser.
     */
    public static void sendTree(Tree tree) {
        //TODO implement this method

        // We receive a tree that each node is the type AbstractSymbol

        // We can use a switch statement to check the type of each node
        // We can use the method getType() to get the type of the node

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


    public void checkTypeCompatibility(Symbol symbol) {
        // Check if the symbol is compatible with the type of the current scope
        /*
        if (symbolTable.currentScope().contains(symbol.getName())) {
            errorHandler.reportError(, symbol.getLineDeclaration(), 0, "Duplicate symbol declaration");
        } else {
            symbolTable.addSymbol(symbol);
        }*/
    }

    /**
     * Function to check if a function is called correctly.
     * @param symbol the symbol to check.
     */
    public void checkFunctionCall(Symbol symbol) {
        // Check if the function is called correctly
        /*
        if (symbolTable.currentScope().contains(symbol.getName())) {
            errorHandler.reportError(, symbol.getLineDeclaration(), 0, "Duplicate symbol declaration");
        } else {
            symbolTable.addSymbol(symbol);
        }*/
	}

    public SemanticAnalyzer(SemanticErrorHandler semanticErrorHandler) {
        errorHandler = semanticErrorHandler;
    }

    public static void sendTree(Tree tree) {

    }

    /**
     * Function to check if the parameters of a function are correct.
     * @param symbol the symbol to check.
     */
    private void checkFunctionParameters(Symbol symbol) {
        // Check if the parameters of the function are correct
        /*
        if (symbolTable.currentScope().contains(symbol.getName())) {
            errorHandler.reportError(, symbol.getLineDeclaration(), 0, "Duplicate symbol declaration");
        } else {
            symbolTable.addSymbol(symbol);
        }*/
    }

    // Additional methods for semantic checks can be added here
}
