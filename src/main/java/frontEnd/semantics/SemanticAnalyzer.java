package frontEnd.semantics;

import errorHandlers.SemanticErrorHandler;
import errorHandlers.errorTypes.SemanticErrorType;
import frontEnd.lexic.dictionary.Token;
import frontEnd.lexic.dictionary.tokenEnums.ValueSymbol;
import frontEnd.semantics.symbolTable.symbol.Symbol;
import frontEnd.semantics.symbolTable.symbol.VariableSymbol;
import frontEnd.sintaxis.Tree;
import frontEnd.semantics.symbolTable.SymbolTableTree;

import java.util.ArrayList;
import java.util.List;

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
    public void sendTree(Tree tree) {
        // We receive a tree that each node is the type AbstractSymbol

        // We can use a switch statement to check the type of each node
        // We can use the method getType() to get the type of the node
        // Get a list of terminal symbols (tokens with lexical meaning).
        List<Token> tokens = new ArrayList<>();

        // Check the first node (root) to see what kind of grammatical operation is done and apply its semantics.
        switch (tree.getNode().toString()) {
            case "declaration":
                break;
            case "assignation":
                checkAssignationSemantics(tokens);
                break;
            // ...
        }
    }

    public void checkAssignationSemantics(List<Token> assignationTokens) {
        // Expected format: VARIABLE IS <value> PUNT_COMMA
        Token variableName = assignationTokens.get(0);
        Token value = assignationTokens.get(2);

        // Check if the current symbol exists.
        Symbol<?> symbol = symbolTable.findSymbol(variableName.getLexeme());
        if (symbol == null) {
            errorHandler.reportError(SemanticErrorType.VARIABLE_NOT_DECLARED, variableName.getLine(), variableName.getColumn(), SemanticErrorType.VARIABLE_NOT_DECLARED.getMessage());
            return;
        }

        // Get the current symbol of this variable to check all its properties.
        if (!symbol.isVariable()) {
            errorHandler.reportError(SemanticErrorType.NOT_A_VARIABLE, variableName.getLine(), variableName.getColumn(), SemanticErrorType.NOT_A_VARIABLE.getMessage());
            return;
        }

        @SuppressWarnings("unchecked")  // Suppress the unchecked cast warning (it will always be a variable and ValueSymbol here)
        Symbol<VariableSymbol<?>> variable = (Symbol<VariableSymbol<?>>) symbol;
        ValueSymbol valueSymbol = (ValueSymbol) value.getType();

        // Check if the value is compatible with the variable type.
        if (!variable.isValidType(valueSymbol)) {
            errorHandler.reportError(SemanticErrorType.INCOMPATIBLE_TYPES, value.getLine(), value.getColumn(), SemanticErrorType.INCOMPATIBLE_TYPES.getMessage());
            return;
        }

        // Assign (and check) the value to the variable.
        variable.setValue(value.getLexeme());
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
