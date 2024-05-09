package frontEnd.semantics;

import errorHandlers.SemanticErrorHandler;
import errorHandlers.errorTypes.SemanticErrorType;
import frontEnd.exceptions.InvalidAssignmentException;
import frontEnd.exceptions.InvalidValueException;
import frontEnd.exceptions.InvalidValueTypeException;
import frontEnd.lexic.dictionary.Token;
import frontEnd.lexic.dictionary.TokenType;
import frontEnd.lexic.dictionary.tokenEnums.BinaryOperator;
import frontEnd.lexic.dictionary.tokenEnums.DataType;
import frontEnd.lexic.dictionary.tokenEnums.ValueSymbol;
import frontEnd.semantics.symbolTable.symbol.Symbol;
import frontEnd.semantics.symbolTable.symbol.VariableSymbol;
import frontEnd.sintaxis.Tree;
import frontEnd.semantics.symbolTable.SymbolTableTree;
import frontEnd.sintaxis.grammar.AbstractSymbol;

import java.util.ArrayList;
import java.util.List;

public class SemanticAnalyzer {
    private final SemanticErrorHandler errorHandler;
    private final SymbolTableTree symbolTable;

    public SemanticAnalyzer(SemanticErrorHandler semanticErrorHandler, SymbolTableTree symbolTable) {
        this.errorHandler = semanticErrorHandler;
        this.symbolTable = symbolTable;
    }

    /**
     * Function to check the semantic of the tree received from the parser.
     * @param tree the tree that we receive from the parser.
     */
    public void sendTree(Tree<AbstractSymbol> tree) {
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
                //generateAddressAssignation()
                break;
            // ...
        }
    }

    // Check if the statement is a valid expression (only one function allowed).
    private boolean hasSingleFunction(List<Token> expressionTokens) {
        for (Token token : expressionTokens) {
            Symbol<?> tokenSymbol = getSymbolByLexeme(token.getLexeme());
            if (!tokenSymbol.isVariable() && expressionTokens.size() > 1) {
                errorHandler.reportError(SemanticErrorType.INVALID_BOOLEAN_EXPRESSION, token.getLine(), token.getColumn(), SemanticErrorType.INVALID_BOOLEAN_EXPRESSION.getMessage());
                return false;
            }
        }
        return true;
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

        // Check what type of assignation this is (depending on the type of the variable being assigned).
        List<Token> expressionTokens = assignationTokens.subList(2, assignationTokens.size() - 2);  // Do not take into account PUNT_COMMA token.

        // Check if the statement is a valid expression (only one function allowed).
        if (!hasSingleFunction(expressionTokens)) {
            return;
        }

        switch (variable.getDataType()) {
            case BOOLEAN -> {
				try {
					checkValidBooleanExpression(expressionTokens);
				} catch (InvalidAssignmentException e) {
					// Do not add the symbol to the symbol table if the expression is invalid.
                    return;
				}
			}
            //case INTEGER, FLOAT -> checkValidNumericExpression(expressionTokens);
            //case STRING -> checkValidStringExpression(expressionTokens);
        }

        // Check if the value is compatible with the variable type and assign (and check) the value to the variable.
		try {
			variable.setValue(value);
		} catch (InvalidValueException e) {
			errorHandler.reportError(SemanticErrorType.INVALID_VALUE, value.getLine(), value.getColumn(), e.getMessage());
		} catch (InvalidValueTypeException e) {
            errorHandler.reportError(SemanticErrorType.INCOMPATIBLE_TYPES, value.getLine(), value.getColumn(), SemanticErrorType.INCOMPATIBLE_TYPES.getMessage());
		}
	}

    private Symbol<?> getSymbolByLexeme(String lexeme) {
        return symbolTable.findSymbol(lexeme);
    }

    private Symbol<?> checkVariableExists(Token token) {
        // Check if the variable exists and it's a boolean.
        Symbol<?> symbol = getSymbolByLexeme(token.getLexeme());
        if (symbol == null) {
            errorHandler.reportError(SemanticErrorType.VARIABLE_NOT_DECLARED, token.getLine(), token.getColumn(), SemanticErrorType.VARIABLE_NOT_DECLARED.getMessage());
        }
        return symbol;
    }

    private boolean checkTokenIsVariable(Token token) {
        Symbol<?> symbol = checkVariableExists(token);
        boolean isVariable = true;
        if (symbol != null && !symbol.isVariable()) {
            isVariable = false;
            errorHandler.reportError(SemanticErrorType.NOT_A_VARIABLE, token.getLine(), token.getColumn(), SemanticErrorType.NOT_A_VARIABLE.getMessage());
        }
        return isVariable;
    }

    private void checkValidBooleanExpression(List<Token> expressionTokens) throws InvalidAssignmentException {
        // Check all the tokens are valid for a boolean expression (e.g. AND, OR, NOT, etc.)
        List<TokenType> validTokens = List.of(ValueSymbol.VALUE_TRUE, ValueSymbol.VALUE_FALSE, ValueSymbol.VARIABLE, BinaryOperator.OR, BinaryOperator.AND, BinaryOperator.NOT);

        boolean validExpression = true;
        for (Token token : expressionTokens) {
            if (!validTokens.contains(token.getType())) {
                validExpression = false;
                errorHandler.reportError(SemanticErrorType.INVALID_BOOLEAN_EXPRESSION, token.getLine(), token.getColumn(), SemanticErrorType.INVALID_BOOLEAN_EXPRESSION.getMessage());
            }
            else if (token.getType() == ValueSymbol.VARIABLE) {
                // Check if the ID (variable or function) exists and it's a boolean.
                Symbol<?> symbol = getSymbolByLexeme(token.getLexeme());
                if (symbol != null && symbol.getDataType() != DataType.BOOLEAN) {
                    validExpression = false;
                    errorHandler.reportError(SemanticErrorType.INCOMPATIBLE_TYPES, token.getLine(), token.getColumn(), SemanticErrorType.INCOMPATIBLE_TYPES.getMessage());
                }
            }
        }

        // Check if the expression is valid
        if (!validExpression) {
           throw new InvalidAssignmentException(SemanticErrorType.INVALID_BOOLEAN_EXPRESSION.getMessage());
        }
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
