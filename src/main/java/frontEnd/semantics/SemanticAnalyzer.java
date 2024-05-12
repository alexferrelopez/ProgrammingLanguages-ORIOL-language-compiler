package frontEnd.semantics;

import errorHandlers.SemanticErrorHandler;
import errorHandlers.errorTypes.SemanticErrorType;
import frontEnd.exceptions.InvalidAssignmentException;
import frontEnd.exceptions.InvalidValueException;
import frontEnd.exceptions.InvalidValueTypeException;
import frontEnd.lexic.dictionary.Token;
import frontEnd.lexic.dictionary.TokenType;
import frontEnd.lexic.dictionary.tokenEnums.*;
import frontEnd.semantics.symbolTable.symbol.FunctionSymbol;
import frontEnd.semantics.symbolTable.symbol.Symbol;
import frontEnd.semantics.symbolTable.symbol.VariableSymbol;
import frontEnd.sintaxis.Tree;
import frontEnd.semantics.symbolTable.SymbolTableTree;
import frontEnd.sintaxis.grammar.AbstractSymbol;
import frontEnd.sintaxis.grammar.derivationRules.TerminalSymbol;

import java.util.ArrayList;
import java.util.List;

public class SemanticAnalyzer {
    private final SemanticErrorHandler errorHandler;
    private final SymbolTableTree symbolTable;

    public SemanticAnalyzer(SemanticErrorHandler semanticErrorHandler, SymbolTableTree symbolTable) {
        this.errorHandler = semanticErrorHandler;
        this.symbolTable = symbolTable;
    }

    private List<Token> convertSymbolsIntoTokens(List<AbstractSymbol> terminalSymbols) {
        List<Token> tokens = new ArrayList<>();

        // Loop through all leave symbols (which can only be terminals or EPSILON).
        for (int i = terminalSymbols.size() - 1; i >= 0; i--) {

            // Loop in inverse order since the first token is in the last terminal read.
            AbstractSymbol symbol = terminalSymbols.get(i);
            if (symbol.isTerminal()) {  // Safe check, not really necessary.
                TerminalSymbol terminal = (TerminalSymbol) symbol;

                // Only get token from the terminals that have any lexical meaning.
                if (!terminal.isEpsilon()) {
                    tokens.add(terminal.getToken());
                }
            }
        }
        return tokens;
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
		List<AbstractSymbol> terminalSymbols = TreeTraversal.getLeafNodesIterative(tree);
		List<Token> tokens = convertSymbolsIntoTokens(terminalSymbols);

        // Check the first node (root) to see what kind of grammatical operation is done and apply its semantics.
        switch (tree.getNode().toString()) {
            case "declaration":
                // Check if it's an assignment or a declaration
                if (tree.getChildren().get(0).getNode().getName().equals("data_type")) {
                    // Check if it's a function call.
                    if (statementIsFuncCall(tree)) {
                        checkFunctionCall(tree, 0);
                    }
                }
                else {
                    // Assignment
                    checkAssignationSemantics(tokens, tree);
                }
                break;
            // ...
        }
    }

    // Main function to determine if "assignation'" node has "var_assignation" or "func_call"
    public <T> boolean statementIsFuncCall(Tree<T> rootNode) {
        for (Tree<T> child : rootNode.getChildren()) {
            // Traverse to find "assignation'" node
            if (child.getNode().equals("assignation")) {
                // Check for "func_call" in the subtree of "assignation'"
				return TreeTraversal.hasSpecificChildType(child, "arg");
            }
        }
        return false;
    }

    public <T> boolean assignmentIsFunction(Tree<T> rootNode) {
        for (Tree<T> child : rootNode.getChildren()) {
            // Traverse to find "assignation'" node
            if (child.getNode().equals("assignation")) {
                // Check for "func_call" in the subtree of "assignation'"
                return TreeTraversal.hasSpecificChildType(child, "arg");
            }
        }
        return false;
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

    public void checkAssignationSemantics(List<Token> assignationTokens, Tree<AbstractSymbol> rootTree) {
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

        // Check if assignment is done with a function.
        if (!variable.isVariable()) {
            // TODO: Check valid function call, it is different than normal statementFuncCall.
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
            Symbol<?> symbol = checkVariableExists(token);
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
        if (symbol == null || !symbol.isVariable()) {
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

            // Check if the token is valid (it is inside the list of "validTokens" which is filled previously).
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

    private void checkLogicalExpression(List<Token> logicalTokens) throws InvalidAssignmentException {
        // Check all the tokens are valid for a boolean expression (e.g. AND, OR, NOT, etc.)
        List<TokenType> validLogicalTokens = List.of(ValueSymbol.VALUE_TRUE, ValueSymbol.VALUE_FALSE, ValueSymbol.VARIABLE, BinaryOperator.OR, BinaryOperator.AND, BinaryOperator.NOT);

        validateLogicalRelationalTokens(logicalTokens, validLogicalTokens);
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

    private Symbol<?> checkFunctionExists(TerminalSymbol variable) {
        String functionName = variable.getToken().getLexeme();
        Symbol<?> functionSymbol = symbolTable.findSymbolGlobally(functionName);

        // Check if the function is declared (exists in the table).
        if (functionSymbol == null || !functionSymbol.isVariable()) {
            errorHandler.reportError(SemanticErrorType.FUNCTION_NOT_DECLARED, variable.getToken().getLine(), variable.getToken().getColumn(), SemanticErrorType.FUNCTION_NOT_DECLARED.getMessage());
        }

        return functionSymbol;
    }

    /**
     * Function to check if a function is called correctly (using DFS for the tree).
     */
    private void checkFunctionCall(Tree<AbstractSymbol> funcCallTree, int currentParameter) {
        // We are on a leaf, check what type of terminal it is.
        if (funcCallTree.getChildren().isEmpty()) {
            TerminalSymbol terminal = (TerminalSymbol) funcCallTree.getNode();

            // Check what terminal we are in to see what part of the statement we are in.
            if (!terminal.isEpsilon()) {

                // Get the name of the variable (or function) and check it's previously declared.
                if (terminal.getToken().getType() == ValueSymbol.VARIABLE) {
                    AbstractSymbol variableParentSymbol = funcCallTree.getParent().getNode();

                    // Get the name of the variable
                    if (variableParentSymbol.getName().equals("assignation")) {
                        checkFunctionExists(terminal);
                    }

                    // Get the name of the parameters
                    if (variableParentSymbol.getName().equals("func_call")) {
                        // Check if the parameter is a function = invalid.
                        String functionName = terminal.getToken().getLexeme();
                        Symbol<?> functionSymbol = symbolTable.findSymbolGlobally(functionName);
                        if (functionSymbol != null && !functionSymbol.isVariable()) {
                            checkFunctionParameters(funcCallTree.getParent(), (FunctionSymbol<?>) functionSymbol, currentParameter);
                        }

                        currentParameter++;
                    }
                }

            }
        }
        for (Tree<AbstractSymbol> child : funcCallTree.getChildren()) {
            checkFunctionCall(child, currentParameter);
        }
	}

    private void checkFunctionAssignment(Tree<AbstractSymbol> funcCallTree, int currentParameter) {
        // We are on a leaf, check what type of terminal it is.
        if (funcCallTree.getChildren().isEmpty()) {
            TerminalSymbol terminal = (TerminalSymbol) funcCallTree.getNode();

            // Check what terminal we are in to see what part of the statement we are in.
            if (!terminal.isEpsilon()) {

                // Get the name of the variable (or function) and check it's previously declared.
                if (terminal.getToken().getType() == ValueSymbol.VARIABLE) {
                    AbstractSymbol variableParentSymbol = funcCallTree.getParent().getNode();

                    // Get the name of the variable
                    if (variableParentSymbol.getName().equals("assignation")) {
                        checkFunctionExists(terminal);
                    }

                    // Get the name of the parameters
                    if (variableParentSymbol.getName().equals("func_call")) {
                        // Check if the parameter is a function = invalid.
                        String functionName = terminal.getToken().getLexeme();
                        Symbol<?> functionSymbol = symbolTable.findSymbolGlobally(functionName);
                        if (functionSymbol != null && !functionSymbol.isVariable()) {
                            checkFunctionParameters(funcCallTree.getParent(), (FunctionSymbol<?>) functionSymbol, currentParameter);
                        }

                        currentParameter++;
                    }
                }

            }
        }
        for (Tree<AbstractSymbol> child : funcCallTree.getChildren()) {
            checkFunctionCall(child, currentParameter);
        }
    }

    /**
     * Function to check if the parameters of a function are correct.
     */
    private void checkFunctionParameters(Tree<AbstractSymbol> functionParameters, FunctionSymbol<?> functionSymbol, int numParameter) {
        // Check there is no function in the parameters.
        Tree<AbstractSymbol> funcParamName = functionParameters.getChildren().get(0);
        Tree<AbstractSymbol> funcParamList = functionParameters.getChildren().get(1);

        // Check if the func_call' statement derives in epsilon (it's okay) or not (calling a func = invalid).
        TerminalSymbol funcList = (TerminalSymbol) funcParamList.getNode();
        if (!funcList.isEpsilon()) {
            errorHandler.reportError(SemanticErrorType.FUNCTION_PARAMETERS_INVALID, funcList.getToken().getLine(), funcList.getToken().getColumn(), SemanticErrorType.FUNCTION_PARAMETERS_INVALID.getMessage());
        }

        // Check if the parameters from the function have the same type as expected (declaration).
        TerminalSymbol parameter = (TerminalSymbol) funcParamName.getNode();
        VariableSymbol<?> expectedParameterSymbol = functionSymbol.getParameters().get(numParameter);
        DataType currentType = getOperandDataType(parameter.getToken());
        DataType expectedType = expectedParameterSymbol.getDataType();

        if (currentType != expectedType) {
            errorHandler.reportError(SemanticErrorType.FUNCTION_PARAMETERS_NOT_MATCH, funcList.getToken().getLine(), funcList.getToken().getColumn(), SemanticErrorType.FUNCTION_PARAMETERS_NOT_MATCH.getMessage());
        }
    }

    // Additional methods for semantic checks can be added here
}
