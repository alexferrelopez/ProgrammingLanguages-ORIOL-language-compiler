package frontEnd.semantics;

import errorHandlers.SemanticErrorHandler;
import errorHandlers.errorTypes.SemanticErrorType;
import frontEnd.exceptions.semantics.InvalidAssignmentException;
import frontEnd.lexic.dictionary.Token;
import frontEnd.lexic.dictionary.TokenType;
import frontEnd.lexic.dictionary.tokenEnums.*;
import frontEnd.semantics.symbolTable.SymbolTableInterface;
import frontEnd.semantics.symbolTable.scope.ScopeType;
import frontEnd.semantics.symbolTable.symbol.FunctionSymbol;
import frontEnd.semantics.symbolTable.symbol.Symbol;
import frontEnd.semantics.symbolTable.symbol.VariableSymbol;
import frontEnd.sintaxis.Tree;
import frontEnd.sintaxis.grammar.AbstractSymbol;
import frontEnd.sintaxis.grammar.derivationRules.TerminalSymbol;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Class to analyze the semantics of the code.
 */
public class SemanticAnalyzer implements SemanticAnalyzerInterface {
    private final SemanticErrorHandler errorHandler;
    private final SymbolTableInterface symbolTable;
    private boolean returnFound = false;
    private boolean mainFound = false;

    public SemanticAnalyzer(SemanticErrorHandler semanticErrorHandler, SymbolTableInterface symbolTable) {
        this.errorHandler = semanticErrorHandler;
        this.symbolTable = symbolTable;
    }

    /**
     * Function to convert the terminal symbols into tokens.
     *
     * @param terminalSymbols a list of terminal symbols.
     * @return a list of tokens.
     */
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
                    // To avoid adding an incomplete tree to the list of tokens
                    if (terminal.getName().equals("CO")) {
                        return tokens;
                    }
                }
            }
        }
        return tokens;
    }

    /**
     * Receive the syntactic tree (from the parser) to analyze it semantically.
     *
     * @param tree Parsing tree (syntactic) to analyze.
     */
    @Override
    public void receiveSyntacticTree(Tree<AbstractSymbol> tree) {
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
                    // Declaration
                    if (statementIsFuncCall(tree)) {
                        checkFunctionCall(tree, 0);
                    } else {
                        checkDeclaration(tokens);
                    }
                } else {
                    // Assignment
                    try {
                        checkAssignationSemantics(tokens, null);
                    } catch (InvalidAssignmentException e) {
                        throw new RuntimeException(e);
                    }
                }
                break;
            // ...
            case "func_type":
                checkFunctionDeclarationSemantics(tokens);
                returnFound = false;
                break;
            case "return_stmt":
                checkReturnSemantics(tokens);
                returnFound = true;
                break;
            case "CT":
                checkCloseBracketsSemantics(tokens);
                symbolTable.leaveCurrentScope();
                break;
            case "loop_for":
                symbolTable.addScope(ScopeType.CONDITIONAL_LOOP);
                checkForSemantics(tokens, tree);
                break;
            case "loop_while", "condition":
                symbolTable.addScope(ScopeType.CONDITIONAL_LOOP);
                checkWhileIfSemantics(tokens);
                break;
            case "ELSE":
                symbolTable.addScope(ScopeType.CONDITIONAL_LOOP);
                break;
            case "EOF":
                checkEOFSematics();
                break;
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
        if (expressionTokens.size() > 1) return false;
        for (Token token : expressionTokens) {
            Symbol<?> tokenSymbol = getSymbolByLexeme(token.getLexeme());
            if (tokenSymbol == null || !tokenSymbol.isVariable()) {
                errorHandler.reportError(SemanticErrorType.INVALID_BOOLEAN_EXPRESSION, token.getLine(), token.getColumn(), expressionTokens.get(0).getLexeme());
                return false;
            }
        }
        return true;
    }

    /**
     * Function to check if an assignation is valid.
     *
     * @param assignationTokens the tokens of the assignation.
     * @param assignedSymbol    the symbol that is being assigned, the variable itself.
     * @throws InvalidAssignmentException when
     */
    private void checkAssignationSemantics(List<Token> assignationTokens, @Nullable Symbol<?> assignedSymbol) throws InvalidAssignmentException {
        // Expected format: VARIABLE IS <value> PUNT_COMMA
        Token variableName = assignationTokens.get(0);

        // Check if the current symbol exists (in case the assigned symbol on a declaration is not passed).
        Symbol<?> symbol = assignedSymbol;
        if (symbol == null) {
            symbol = symbolTable.findSymbol(variableName.getLexeme());
            if (symbol == null) {
                errorHandler.reportError(SemanticErrorType.VARIABLE_NOT_DECLARED, variableName.getLine(), variableName.getColumn(), variableName.getLexeme());
                throw new InvalidAssignmentException(SemanticErrorType.VARIABLE_NOT_DECLARED.getMessage());
            }
        }

        // Get the current symbol of this variable to check all its properties.
        if (!symbol.isVariable()) {
            errorHandler.reportError(SemanticErrorType.ALREADY_USED_IDENTIFIER, variableName.getLine(), variableName.getColumn(), variableName.getLexeme());
            throw new InvalidAssignmentException(SemanticErrorType.VARIABLE_NOT_DECLARED.getMessage());
        }

        @SuppressWarnings("unchecked")  // Suppress the unchecked cast warning (it will always be a variable and ValueSymbol here)
        Symbol<VariableSymbol<?>> variable = (Symbol<VariableSymbol<?>>) symbol;

        // Check what type of assignation this is (depending on the type of the variable being assigned).
        int indexOfFirstSeparator = getIndexOfFirstSeparator(assignationTokens, SpecialSymbol.PUNT_COMMA);

        List<Token> expressionTokens = assignationTokens.subList(2, Math.max(indexOfFirstSeparator, assignationTokens.size() - 1));  // Do not take into account PUNT_COMMA token.

        // Check if the statement is a valid expression (only one function allowed).
        //TODO check the function when the expression tokens only contain a literal, and not a function
        if (checkIfFunctionExists(expressionTokens.get(0))) {
            if (!hasSingleFunction(expressionTokens)) {
                throw new InvalidAssignmentException(SemanticErrorType.VARIABLE_NOT_DECLARED.getMessage());
            }
        }

        // Check if assignment is done with a function.
        if (!variable.isVariable()) {
            // TODO: Check valid function call as assignment, it is different than normal statementFuncCall.
        }

        switch (variable.getDataType()) {
            case BOOLEAN -> {
                try {
                    checkValidBooleanExpression(expressionTokens);
                } catch (InvalidAssignmentException e) {
                    // Do not add the symbol to the symbol table if the expression is invalid.
                    throw new InvalidAssignmentException(SemanticErrorType.VARIABLE_NOT_DECLARED.getMessage());
                }
            }
            case INTEGER, FLOAT -> checkValidArithmeticExpression(expressionTokens, variable);
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
     * - Check if the expression is valid (e.g. 1 + 2 or 2 * 4).
     * - Check if the variables are declared and are numbers (integers or floats).
     * - Check if the operations are done between the same type of variables.
     * - Check if there is a division by zero (just in literal numbers, not with variables).
     *
     * @param expressionTokens the tokens of the arithmetic expression.
     */
    private void checkValidArithmeticExpression(List<Token> expressionTokens, Symbol<VariableSymbol<?>> variableSymbol) {
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
                errorHandler.reportError(SemanticErrorType.INVALID_ARITHMETIC_EXPRESSION, token.getLine(), token.getColumn(), token.getLexeme());
                isValid = false;
            } else if (token.getType() == ValueSymbol.VARIABLE) {
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
                    errorHandler.reportError(SemanticErrorType.INCOMPATIBLE_TYPES, token.getLine(), token.getColumn(), previousToken.getLexeme() + " " + nextToken.getLexeme());
                    isValid = false;
                }
                // Check if the operand has the same type as the variable.
                else if (leftOperandType != variableSymbol.getDataType()) {
                    errorHandler.reportError(SemanticErrorType.INCOMPATIBLE_TYPES, token.getLine(), token.getColumn(), leftOperandType + " " + variableSymbol);
                    isValid = false;
                }
            }
        }

        // Check if the operation was semantically correct (or not) to warn.
        if (!isValid) {
            // TODO: Send a warning to the user that the arithmetic expression is invalid (throw exception).
        }
    }

    // Method to determine the data type of token.
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
        // Check if the variable exists.
        Symbol<?> symbol = getSymbolByLexeme(token.getLexeme());
        if (symbol == null || !symbol.isVariable()) {
            errorHandler.reportError(SemanticErrorType.VARIABLE_NOT_DECLARED, token.getLine(), token.getColumn(), token.getLexeme());
        }
        return symbol;
    }

    /**
     * @param token
     * @return
     */
    private boolean checkTokenIsVariable(Token token) {
        Symbol<?> symbol = checkVariableExists(token);
        boolean isVariable = true;
        if (symbol != null && !symbol.isVariable()) {
            isVariable = false;
            errorHandler.reportError(SemanticErrorType.ALREADY_USED_IDENTIFIER, token.getLine(), token.getColumn(), token.getLexeme());
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
                errorHandler.reportError(SemanticErrorType.INVALID_BOOLEAN_EXPRESSION, token.getLine(), token.getColumn(), token.getLexeme());
            } else if (token.getType() == ValueSymbol.VARIABLE) {
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
        // Check if the ID (variable or function) exists.
        Symbol<?> symbol = getSymbolByLexeme(token.getLexeme());
        if (symbol != null && !dataTypes.contains(symbol.getDataType())) {
            errorHandler.reportError(SemanticErrorType.INCOMPATIBLE_TYPES, token.getLine(), token.getColumn(), "Expected: " + dataTypes + " but received: " + token.getLexeme());
            return false;
        }
        return true;
    }

    /**
     * Function to check if a symbol is declared in the current scope.
     */
    private void checkDeclaration(List<Token> declarationTokens) {
        // DECLARATION = DATA_TYPE VARIABLE IS <VALUE> PUNT_COMMA;
        Token variableDatatype = declarationTokens.get(0);
        Token variable = declarationTokens.get(1);

        // Check if the symbol is already declared in the scope.
        if (symbolTable.containsSymbol(variable.getLexeme())) {
            errorHandler.reportError(SemanticErrorType.DUPLICATE_SYMBOL_DECLARATION, variable.getLine(), variable.getColumn(), variable.getLexeme());
            return;
        }

        // Add the symbol to the scope (with it's data type).
        DataType dataType = (DataType) variableDatatype.getType();
        // TODO: Type is Integer but Generics may be removed if the value of the Symbol is never stored.
        VariableSymbol<Integer> variableSymbol = new VariableSymbol<>(variable.getLexeme(), dataType, variableDatatype.getLine(), false, Integer.class);

        List<Token> assignationTokens = declarationTokens.subList(1, declarationTokens.size());    // Skip DATA_TYPE token.
        try {
            // Check the assignments of the variable and add it to the table.
            checkAssignationSemantics(assignationTokens, variableSymbol);
            symbolTable.addSymbol(variableSymbol);

        } catch (InvalidAssignmentException e) {
            // Do not the symbol from the table.
        }
    }


    private void checkTypeCompatibility(Symbol symbol) {
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
            errorHandler.reportError(SemanticErrorType.FUNCTION_NOT_DECLARED, variable.getToken().getLine(), variable.getToken().getColumn(), functionName);
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
            errorHandler.reportError(SemanticErrorType.FUNCTION_PARAMETERS_INVALID, funcList.getToken().getLine(), funcList.getToken().getColumn(), funcList.getName());
        }

        // Check if the parameters from the function have the same type as expected (declaration).
        TerminalSymbol parameter = (TerminalSymbol) funcParamName.getNode();
        VariableSymbol<?> expectedParameterSymbol = functionSymbol.getParameters().get(numParameter);
        DataType currentType = getOperandDataType(parameter.getToken());
        DataType expectedType = expectedParameterSymbol.getDataType();

        if (currentType != expectedType) {
            errorHandler.reportError(SemanticErrorType.FUNCTION_PARAMETERS_DONT_MATCH, funcList.getToken().getLine(), funcList.getToken().getColumn(), funcList.getName());
        }
    }

    /**
     * Check the semantics after finishing
     */
    private void checkEOFSematics() {
        if (!mainFound) {
            errorHandler.reportError(SemanticErrorType.MAIN_FUNCTION_MISSING, null, null, "");
        }
    }

    /**
     * Check the semantics of a closing bracket
     *
     * @param tokens the tokens of the closing bracket
     */
    private void checkCloseBracketsSemantics(List<Token> tokens) {
        if (symbolTable.getCurrentScope().getScopeType() == ScopeType.FUNCTION) {
            if (!returnFound) {
                errorHandler.reportError(SemanticErrorType.RETURN_STATEMENT_MISSING, tokens.get(0).getLine(), null, "");
            }
        }
    }

    /**
     * Check the semantics of a return declaration
     *
     * @param tokens the tokens of the return declaration
     */
    private void checkReturnSemantics(List<Token> tokens) {
        Token token = tokens.get(1);
        if (token.getLexeme().equals("void")) {
            DataType functionReturnType = symbolTable.getCurrentScope().getReturnType();
            if (functionReturnType != DataType.VOID) {
                errorHandler.reportError(SemanticErrorType.FUNCTION_RETURN_TYPE_NOT_CORRECT, token.getLine(), null, "Expected " + functionReturnType + " but received void");
            }
        } else {
            ValueSymbol type = (ValueSymbol) token.getType();
            DataType returnType = null;
            if (type == ValueSymbol.VARIABLE) {
                Symbol<?> symbol = symbolTable.findSymbol(token.getLexeme());
                if (Objects.isNull(symbol)) {
                    errorHandler.reportError(SemanticErrorType.VARIABLE_NOT_DECLARED, token.getLine(), null, "Variable " + token.getLexeme() + " not declared");
                }
                returnType = symbol.getDataType();
            } else {
                returnType = switch (type) {
                    case VALUE_INT -> DataType.INTEGER;
                    case VALUE_FLOAT -> DataType.FLOAT;
                    case VALUE_TRUE, VALUE_FALSE -> DataType.BOOLEAN;
                    case VALUE_CHAR -> DataType.CHAR;
                    case VALUE_STRING -> DataType.STRING;
                    default -> returnType;
                };
            }

            DataType functionReturnType = symbolTable.getCurrentScope().getReturnType();
            if (returnType != functionReturnType) {
                errorHandler.reportError(SemanticErrorType.FUNCTION_RETURN_TYPE_NOT_CORRECT, token.getLine(), null, "Return type is not correct expected " + functionReturnType + " but received " + returnType);
            }
        }
    }


    private void checkFunctionDeclarationSemantics(List<Token> tokens) {

        //Obtain the parameters of the function
        int indexOfFirstSeparator = getIndexOfFirstSeparator(tokens, SpecialSymbol.CT);
        List<Token> functionDeclarationTokens = tokens.subList(0, indexOfFirstSeparator);
        List<VariableSymbol<?>> functionParameters = getFunctionParameters(functionDeclarationTokens);

        //Check if the parameters have unique names
        checkUniqueVariableNames(functionParameters);
        //Check if the function is already defined
        checkIfFunctionExists(tokens.get(2));

        //Obtain information about the function
        String functionName = tokens.get(2).getLexeme();
        DataType dataTypeReturn = (DataType) tokens.get(0).getType();
        long lineDeclaration = tokens.get(0).getLine();

        FunctionSymbol functionSymbol = new FunctionSymbol(functionName, dataTypeReturn, functionParameters, lineDeclaration, null);

        symbolTable.addSymbol(functionSymbol);
        symbolTable.addScope(ScopeType.FUNCTION, dataTypeReturn);

        //Add the parameters to the symbol table
        for (VariableSymbol<?> parameter : functionParameters) {
            symbolTable.addSymbol(parameter);
        }

        checkMainFunction(tokens);

    }

    /**
     * Check the semantics of the main function
     * 1.- check that there is only one main
     * 2.- Check that the first token is a miau
     * 3.- Check that the 4th token is a )
     *
     * @param tokens list of tokens of the main function
     */
    private void checkMainFunction(List<Token> tokens) {
        Token mainToken = tokens.get(2);
        if (mainToken.getType() == ReservedSymbol.MAIN) { //Check if the function is main
            if (mainFound) { //Check if the main function is already defined
                errorHandler.reportError(SemanticErrorType.MAIN_FUNCTION_ALREADY_DEFINED, mainToken.getLine(), mainToken.getColumn(), mainToken.getLexeme());
            } else {
                mainFound = true;
                if (!tokens.get(0).getLexeme().equals("miau")) { //Check if the return type is miau (int)
                    errorHandler.reportError(SemanticErrorType.MAIN_FUNCTION_RETURN_TYPE, tokens.get(0).getLine(), tokens.get(0).getColumn(), tokens.get(0).getLexeme());
                }
                if (!tokens.get(4).getLexeme().equals(")")) { //Check if the main function has parameters
                    errorHandler.reportError(SemanticErrorType.MAIN_FUNCTION_PARAMETERS, tokens.get(4).getLine(), tokens.get(4).getColumn(), tokens.get(4).getLexeme());
                }
            }
        }
    }

    private void checkUniqueVariableNames(List<VariableSymbol<?>> variables) {
        for (int i = 0; i < variables.size(); i++) {
            for (int j = i + 1; j < variables.size(); j++) {
                if (variables.get(i).getName().equals(variables.get(j).getName())) {
                    errorHandler.reportError(SemanticErrorType.VARIABLE_ALREADY_DEFINED, (int) variables.get(i).getLineDeclaration(), null, variables.get(i).getName());
                }
            }
        }
    }

    /**
     * Check the semantics of a while or if statement
     *
     * @param whileIfTokens the tokens of the while or if statement
     */
    public void checkWhileIfSemantics(List<Token> whileIfTokens) {
        // Expected format: WHILE (<boolean_expression>) {}
        int indexLastTokenInCondition = getIndexOfFirstSeparator(whileIfTokens, SpecialSymbol.PT);
        List<Token> expressionTokens = whileIfTokens.subList(2, indexLastTokenInCondition);

        // Check if the condition expression is valid.
        try {
            checkValidBooleanExpression(expressionTokens);
            //TODO change the exception
        } catch (InvalidAssignmentException e) {
            errorHandler.reportError(SemanticErrorType.INVALID_BOOLEAN_EXPRESSION, expressionTokens.get(0).getLine(), expressionTokens.get(0).getColumn(), expressionTokens.get(0).getLexeme());
        }
    }

    /**
     * Check the semantics of a for statement
     *
     * @param forTokens the tokens of the for statement
     * @param tree      the tree of the for statement
     */
    public void checkForSemantics(List<Token> forTokens, Tree<AbstractSymbol> tree) {
        // Expected format: FOR (<declaration> TO <literal_num>, <assignation> ) {}

        int indexLastTokenInCondition = getIndexOfFirstSeparator(forTokens, ReservedSymbol.TO);
        List<Token> declarationTokens = forTokens.subList(2, indexLastTokenInCondition);

        // Check if it's an assignment or a declaration
        Tree<AbstractSymbol> abstractSymbolTree = tree.getChildren().get(2);

        String firstTokenName = abstractSymbolTree.getChildren().get(0).getNode().getName();

        /* The declared data type of the declaration/assignation to check the type of the limit value */
        DataType declaredType = null;

        // Case where the first token starts a declaration
        if (firstTokenName.equals("data_type")) {
            declaredType = (DataType) declarationTokens.get(0).getType();
            // check if the declaration is a function call
            Symbol<?> symbol = symbolTable.findSymbolGlobally(declarationTokens.get(1).getLexeme());

            if (symbol == null) {
                checkDeclaration(declarationTokens);
            } else if (!symbol.isVariable()) {
                errorHandler.reportError(SemanticErrorType.ALREADY_USED_IDENTIFIER, declarationTokens.get(1).getLine(), declarationTokens.get(1).getColumn(), declarationTokens.get(1).getLexeme());
            } else {
                errorHandler.reportError(SemanticErrorType.VARIABLE_ALREADY_DEFINED, declarationTokens.get(1).getLine(), declarationTokens.get(1).getColumn(), declarationTokens.get(1).getLexeme());
            }
            // Case where the first token starts an assignment
        } else {
            Symbol<?> symbolGlobally = symbolTable.findSymbolGlobally(declarationTokens.get(0).getLexeme());
            if (symbolGlobally == null) {
                errorHandler.reportError(SemanticErrorType.VARIABLE_NOT_DECLARED, declarationTokens.get(0).getLine(), declarationTokens.get(0).getColumn(), declarationTokens.get(0).getLexeme());
            } else {
                try {
                    declaredType = symbolGlobally.getDataType();
                    checkAssignationSemantics(declarationTokens, null);
                } catch (InvalidAssignmentException ignored) {
                    // Error handler already reported the error inside the method.
                }
            }
        }

        Token limitValue = forTokens.get(indexLastTokenInCondition + 1);
        //TODO check if its a number or a variable, check their type against the variable declared before

        TokenType limitValueType = limitValue.getType();
        if (limitValueType == ValueSymbol.VARIABLE) {
            Symbol<?> symbol = checkVariableExists(limitValue);
            if (symbol != null) {           // Variable exists
                boolean declarationAndLimitAreSameType = false;
                if (declaredType != null) {
                    declarationAndLimitAreSameType = checkVariableSameType(limitValue, List.of(declaredType));
                }
                if (!declarationAndLimitAreSameType) {
                    errorHandler.reportError(SemanticErrorType.INCOMPATIBLE_TYPES, limitValue.getLine(), limitValue.getColumn(), limitValue.getLexeme());
                }
            } else {                        // Variable doesn't exist
                errorHandler.reportError(SemanticErrorType.VARIABLE_NOT_DECLARED, limitValue.getLine(), limitValue.getColumn(), limitValue.getLexeme());
            }
        } else {
            // Check if the numeric value is the same type as the variable declared.
            if (limitValueType != declaredType) {
                //todo check equivalence between the literals and variable types
                errorHandler.reportError(SemanticErrorType.INCOMPATIBLE_TYPES, limitValue.getLine(), limitValue.getColumn(), limitValue.getLexeme());
            }
        }

        // Check if the numeric value is the same type as the variable declared.

        int indexLastTokenUntilSeparator = getIndexOfFirstSeparator(forTokens, SpecialSymbol.PT);
        List<Token> assignationTokens = forTokens.subList(indexLastTokenInCondition + 3, indexLastTokenUntilSeparator);
        try {
            checkAssignationSemantics(assignationTokens, null);
        } catch (InvalidAssignmentException e) {

        }
    }

    private int getIndexOfFirstSeparator(List<Token> tokenList, TokenType separator) {
        int indexLastTokenInCondition = tokenList.size();
        for (int i = 0; i < tokenList.size(); i++) {
            Token ifToken = tokenList.get(i);
            if (ifToken.getType() == separator) {
                indexLastTokenInCondition = i;
                break;
            }
        }
        return indexLastTokenInCondition;
    }

    private List<VariableSymbol<?>> getFunctionParameters(List<Token> tokens) {
        List<VariableSymbol<?>> parameters = new ArrayList<>();
        boolean startParams = false;

        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            if (Objects.isNull(token)) continue;
            switch (token.getLexeme()) {
                case "(":
                    startParams = true;
                    break;
                case ")":
                    startParams = false;
                    break;
                case ",":
                    break;
                default:
                    if (startParams) {
                        TokenType tt = token.getType();
                        if (token.getType().toString().equals("VARIABLE")) {
                            String name = token.getLexeme();
                            long lineDeclaration = token.getLine();
                            DataType dt = (DataType) tokens.get(i - 1).getType();

                            parameters.add(new VariableSymbol<>(name, dt, lineDeclaration, true, null));
                        }

                        System.out.println(tt.toString());
                    }
            }
        }
        return parameters;
    }

    private boolean checkIfFunctionExists(Token token) {
        Symbol<?> symbol = symbolTable.findSymbolGlobally(token.getLexeme());
        return (symbol != null && !symbol.isVariable());
    }
}