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
import frontEnd.lexic.dictionary.tokenEnums.MathOperator;
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
                // Check if it's an assignment or a declaration
                if (tree.getChildren().get(0).getNode().getName().equals("data_type")) {
                    // Declaration
                }
                else {
                    // Assignment
                    checkAssignationSemantics(tokens);
                }
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
            case INTEGER, FLOAT -> checkValidArithmeticExpression(expressionTokens, variable.getDataType());
            //case STRING -> checkValidStringExpression(expressionTokens);
        }

        // Check if the value is compatible with the variable type and assign (and check) the value to the variable.
        /*
		try {
			variable.checkValue(value);
		} catch (InvalidValueException e) {
			errorHandler.reportError(SemanticErrorType.INVALID_VALUE, variableName.getLine(), variableName.getColumn(), e.getMessage());
		} catch (InvalidValueTypeException e) {
            errorHandler.reportError(SemanticErrorType.INCOMPATIBLE_TYPES, variableName.getLine(), variableName.getColumn(), SemanticErrorType.INCOMPATIBLE_TYPES.getMessage());
		}
		*/
	}

    /**
     * Function to check if the arithmetic expression is valid.
     *  - Check if the expression is valid (e.g. 1 + 2 or 2 * 4).
     *  - Check if the variables are declared and are numbers (integers or floats).
     *  - Check if the operations are done between the same type of variables.
     *  - Check if there is a division by zero (just in literal numbers, not with variables).
     * @param expressionTokens  the tokens of the arithmetic expression.
     */
    private void checkValidArithmeticExpression(List<Token> expressionTokens, DataType variableDataType) {
        // Check all the tokens are valid for an arithmetic expression (e.g. +, -, *, /, etc.)
        List<TokenType> validArithmeticOperatorsTokens = List.of(MathOperator.SUM, MathOperator.SUB, MathOperator.MUL, MathOperator.DIV, MathOperator.POW, MathOperator.MOD);
        List<TokenType> validArithmeticValueTokens = List.of(ValueSymbol.VALUE_INT, ValueSymbol.VALUE_FLOAT, ValueSymbol.VARIABLE);

        List<TokenType> validArithmeticTokens = new ArrayList<>();
        validArithmeticTokens.addAll(validArithmeticValueTokens);
        validArithmeticTokens.addAll(validArithmeticOperatorsTokens);
        boolean isValid = true;

        for (Token token : expressionTokens) {
            // Check if the token is inside the valid arithmetic tokens.
            if (!validArithmeticTokens.contains(token.getType())) {
                errorHandler.reportError(SemanticErrorType.INVALID_ARITHMETIC_EXPRESSION, token.getLine(), token.getColumn(), SemanticErrorType.INVALID_ARITHMETIC_EXPRESSION.getMessage());
                isValid = false;
            }
            else if (token.getType() == ValueSymbol.VARIABLE) {
                // Check if the ID (variable or function) exists and it's a number (integer or float).
                if (!checkVariableSameType(token, List.of(DataType.INTEGER, DataType.FLOAT))) {
                    isValid = false;
                }
            }

            // Check the operation (sum, sub...) is done between same type of variables / values.
            if (isValid == validArithmeticOperatorsTokens.contains(token.getType())) {
                // Check if the previous and next tokens are compatible.
                int tokenIndex = expressionTokens.indexOf(token);
                Token previousToken = expressionTokens.get(tokenIndex - 1);
                Token nextToken = expressionTokens.get(tokenIndex + 1);

                DataType leftOperandType = getOperandDataType(previousToken);
                DataType rightOperandType = getOperandDataType(nextToken);

                // Check if both operands have same type.
                if (leftOperandType != rightOperandType) {
                    errorHandler.reportError(SemanticErrorType.INCOMPATIBLE_TYPES, token.getLine(), token.getColumn(), SemanticErrorType.INCOMPATIBLE_TYPES.getMessage());
                    isValid = false;
                }
                // Check if the operand has the same type as the variable.
                else if (leftOperandType != variableDataType) {
                    errorHandler.reportError(SemanticErrorType.INCOMPATIBLE_TYPES, token.getLine(), token.getColumn(), SemanticErrorType.INCOMPATIBLE_TYPES.getMessage());
                    isValid = false;
                }
            }
        }

        // Check if the operation was semantically correct (or not) to warn.
        if (!isValid) {
            // TODO: Send a warning to the user that the arithmetic expression is invalid (throw exception).
        }
    }

    // Method to determine the data type of a token.
    private DataType getOperandDataType(Token token) {
        if (token.getType() == ValueSymbol.VARIABLE) {
            Symbol<?> symbol = getSymbolByLexeme(token.getLexeme());
            return symbol.getDataType();
        } else if (token.getType() instanceof ValueSymbol) {
            return ((ValueSymbol) token.getType()).getDataType();
        } else {
            // This case should never happen.
            throw new IllegalArgumentException("Unsupported token type for arithmetic operations");
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
        checkLogicalExpression(expressionTokens);
        checkRelationalExpression(expressionTokens);
    }

    private void validateLogicalRelationalTokens(List<Token> expressionTokens, List<TokenType> validTokens) throws InvalidAssignmentException {
        boolean validExpression = true;
        for (Token token : expressionTokens) {
            if (!validTokens.contains(token.getType())) {
                validExpression = false;
                errorHandler.reportError(SemanticErrorType.INVALID_BOOLEAN_EXPRESSION, token.getLine(), token.getColumn(), SemanticErrorType.INVALID_BOOLEAN_EXPRESSION.getMessage());
            }
            else if (token.getType() == ValueSymbol.VARIABLE) {
                // Check if the ID (variable or function) exists and it's a boolean.
                if (!checkVariableSameType(token, List.of(DataType.BOOLEAN))) {
                    validExpression = false;
                }
            }
        }

        // Check if the expression is valid
        if (!validExpression) {
            throw new InvalidAssignmentException(SemanticErrorType.INVALID_BOOLEAN_EXPRESSION.getMessage());
        }
    }

    private void checkRelationalExpression(List<Token> relationalTokens) throws InvalidAssignmentException {
        // Check all the tokens are valid for a boolean expression (e.g. AND, OR, NOT, etc.)
        List<TokenType> validRelationalTokens = List.of(ValueSymbol.VALUE_TRUE, ValueSymbol.VALUE_FALSE, ValueSymbol.VARIABLE, BinaryOperator.GT, BinaryOperator.LT, BinaryOperator.EQ, BinaryOperator.NEQ);
        validateLogicalRelationalTokens(relationalTokens, validRelationalTokens);
    }

        // Check if the expression is valid
        if (!validExpression) {
           throw new InvalidAssignmentException(SemanticErrorType.INVALID_BOOLEAN_EXPRESSION.getMessage());
        }
    }

    private boolean checkVariableSameType(Token token, List<DataType> dataTypes) {
        // Check if the ID (variable or function) exists and it's a boolean.
        Symbol<?> symbol = getSymbolByLexeme(token.getLexeme());
        if (symbol != null && !dataTypes.contains(symbol.getDataType())) {
            errorHandler.reportError(SemanticErrorType.INCOMPATIBLE_TYPES, token.getLine(), token.getColumn(), SemanticErrorType.INCOMPATIBLE_TYPES.getMessage());
            return false;
        }
        return true;
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
