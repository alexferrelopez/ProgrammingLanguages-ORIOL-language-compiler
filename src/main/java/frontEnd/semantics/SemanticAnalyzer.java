package frontEnd.semantics;

import errorHandlers.SemanticErrorHandler;
import errorHandlers.errorTypes.SemanticErrorType;
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
    public void checkSyntacticTree(Tree<AbstractSymbol> tree) {
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
                    checkDeclaration(tokens);
                } else {
                    // Assignment
                    if (symbolTable.containsSymbolGlobally(tokens.get(0).getLexeme())) {
                        String lexeme = tokens.get(0).getLexeme();
                        Symbol<?> symbolByLexeme = symbolTable.findSymbolGlobally(lexeme);

                        if (symbolByLexeme.isFunction()) {
                            boolean functionExists = checkIfFunctionExists(tokens.get(0));
                            if (functionExists) {
                                checkLonelyFunctionCall(tokens);
                            } else {
                                errorHandler.reportError(SemanticErrorType.FUNCTION_NOT_DECLARED, tokens.get(0).getLine(), tokens.get(0).getColumn(), tokens.get(0).getLexeme());
                            }
                        } else {
                            checkAssignationSemantics(tokens, symbolByLexeme);
                        }
                    } else {
                        errorHandler.reportError(SemanticErrorType.UNEXPECTED_TOKEN_ASSIGNATION, tokens.get(0).getLine(), tokens.get(0).getColumn(), tokens.get(0).getLexeme());
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

    /**
     * Function to check if an assignation is valid.
     *
     * @param assignationTokens the tokens of the assignation.
     * @param assignedSymbol    the symbol that is being assigned, the variable itself.
     * @return a ResultContainer with the result of the assignation.
     */
    private ResultContainer checkAssignationSemantics(List<Token> assignationTokens, @Nullable Symbol<?> assignedSymbol) {
        // Expected format: VARIABLE IS <value> PUNT_COMMA
        Token variableName = assignationTokens.get(0);
        boolean failed = false;

        // Check if the current symbol exists (in case the assigned symbol on a declaration is not passed).
        Symbol<?> symbol = assignedSymbol;
        if (symbol == null) {
            symbol = symbolTable.findSymbolGlobally(variableName.getLexeme());
            if (symbol == null) {
                errorHandler.reportError(SemanticErrorType.VARIABLE_NOT_DECLARED, variableName.getLine(), variableName.getColumn(), variableName.getLexeme());
                return new ResultContainer(true, true, null, null, null, "");
            }
        }

        // Get the current symbol of this variable to check all its properties.
        if (symbol.isFunction()) {
            errorHandler.reportError(SemanticErrorType.ALREADY_USED_IDENTIFIER, variableName.getLine(), variableName.getColumn(), variableName.getLexeme());
            failed = true;
        }

        @SuppressWarnings("unchecked")  // Suppress the unchecked cast warning (it will always be a variable and ValueSymbol here)
        Symbol<VariableSymbol<?>> variable = (Symbol<VariableSymbol<?>>) symbol;
        DataType variableDataType = variable.getDataType();

        // Check what type of assignation this is (depending on the type of the variable being assigned).
        int indexOfFirstSeparator = getIndexOfFirstSeparator(assignationTokens, SpecialSymbol.PUNT_COMMA);

        List<Token> expressionTokens = assignationTokens.subList(2, Math.max(indexOfFirstSeparator, assignationTokens.size() - 1));  // Do not take into account PUNT_COMMA token.

        // Check if the statement is a valid expression (only one function allowed).
        if (checkIfFunctionExists(expressionTokens.get(0))) {
            // check only one function is called
            int firstParenthesisIndex = getIndexOfFirstSeparator(expressionTokens, SpecialSymbol.PT);

            // The first parenthesis found is just before the last token, which is the ";" symbol.
            if (firstParenthesisIndex == expressionTokens.size() - 1) {
                //check if the function is declared and the return type is the same as the variable
                FunctionSymbol<?> functionSymbol = (FunctionSymbol<?>) symbolTable.findSymbolGlobally(expressionTokens.get(0).getLexeme());

                if (functionSymbol.getDataType() == variableDataType) {
                    List<VariableSymbol<?>> expectedParams = functionSymbol.getParameters();

                    if (expectedParams.size() > 4) {
                        errorHandler.reportError(SemanticErrorType.MAXIMUM_PARAMETERS_EXCEEDED, expressionTokens.get(0).getLine(), expressionTokens.get(0).getColumn(), "expected a maximum of 4 but received: " + expectedParams.size());
                        failed = true;
                    }

                    List<VariableSymbol<?>> receivedParams = getFunctionCallParameters(expressionTokens);

                    if (expectedParams.size() == receivedParams.size()) {
                        for (int i = 0; i < expectedParams.size(); i++) {
                            if (expectedParams.get(i) == null || receivedParams.get(i) == null || expectedParams.get(i).getDataType() != receivedParams.get(i).getDataType()) {
                                errorHandler.reportError(SemanticErrorType.FUNCTION_PARAMETERS_DONT_MATCH, expressionTokens.get(0).getLine(), expressionTokens.get(0).getColumn(), "expected: " + expectedParams.get(i).getDataType() + " but received: " + receivedParams.get(i).getDataType());
                                failed = true;
                            }
                        }
                    } else {
                        errorHandler.reportError(SemanticErrorType.FUNCTION_PARAMETERS_NUMBER_INCORRECT, expressionTokens.get(0).getLine(), expressionTokens.get(0).getColumn(), "expected: " + expectedParams.size() + " but received: " + receivedParams.size());
                        failed = true;
                    }
                } else {
                    errorHandler.reportError(SemanticErrorType.INCOMPATIBLE_TYPES, expressionTokens.get(0).getLine(), expressionTokens.get(0).getColumn(), "expected: " + variableDataType + " but received: " + functionSymbol.getDataType());
                    failed = true;
                }
            } else {
                errorHandler.reportError(SemanticErrorType.FUNCTION_NOT_CALLED_CORRECTLY, expressionTokens.get(0).getLine(), expressionTokens.get(0).getColumn(), expressionTokens.get(0).getLexeme());
                failed = true;
            }
        } else {
            switch (variableDataType) {
                case BOOLEAN -> {
                    ResultContainer booleanExpressionEval = checkValidBooleanExpression(expressionTokens);
                    handleResultContainer(booleanExpressionEval);
                    if (booleanExpressionEval.isError()) failed = true;
                }
                case INTEGER -> {
                    ResultContainer resultContainer = checkValidArithmeticExpression(expressionTokens, variable, DataType.INTEGER, ValueSymbol.VALUE_INT);
                    handleResultContainer(resultContainer);
                    if (resultContainer.isError()) failed = true;
                }
                case FLOAT -> {
                    ResultContainer resultContainer = checkValidArithmeticExpression(expressionTokens, variable, DataType.FLOAT, ValueSymbol.VALUE_FLOAT);
                    handleResultContainer(resultContainer);
                    if (resultContainer.isError()) failed = true;
                }
            }
        }
        return new ResultContainer(failed, true, null, null, null, "");
    }

    /**
     * Function to check if a function is declared. Exclusive to functions that are not assigned to a variable.
     * Checks if the function is declared and if the parameters are correct.
     *
     * @param expressionTokens the tokens of the function call.
     */
    private void checkLonelyFunctionCall(List<Token> expressionTokens) {
        int indexOfFirstSeparator = getIndexOfFirstSeparator(expressionTokens, SpecialSymbol.PUNT_COMMA);
        expressionTokens = expressionTokens.subList(0, Math.max(indexOfFirstSeparator, expressionTokens.size() - 1));  // Do not take into account PUNT_COMMA token.

        boolean failed = false;
        // check only one function is called
        int firstParenthesisIndex = getIndexOfFirstSeparator(expressionTokens, SpecialSymbol.PT);

        // The first parenthesis found is just before the last token, which is the ";" symbol.
        if (firstParenthesisIndex == expressionTokens.size() - 1) {
            //check if the function is declared and the return type is the same as the variable
            FunctionSymbol<?> functionSymbol = (FunctionSymbol<?>) symbolTable.findSymbolGlobally(expressionTokens.get(0).getLexeme());


            List<VariableSymbol<?>> expectedParams = functionSymbol.getParameters();

            if (expectedParams.size() > 4) {
                errorHandler.reportError(SemanticErrorType.MAXIMUM_PARAMETERS_EXCEEDED, expressionTokens.get(0).getLine(), expressionTokens.get(0).getColumn(), "expected a maximum of 4 but received: " + expectedParams.size());
                failed = true;
            }

            List<VariableSymbol<?>> receivedParams = getFunctionCallParameters(expressionTokens);

            if (expectedParams.size() == receivedParams.size()) {
                for (int i = 0; i < expectedParams.size(); i++) {
                    if (expectedParams.get(i).getDataType() != receivedParams.get(i).getDataType()) {
                        errorHandler.reportError(SemanticErrorType.FUNCTION_PARAMETERS_DONT_MATCH, expressionTokens.get(0).getLine(), expressionTokens.get(0).getColumn(), "expected: " + expectedParams.get(i).getDataType() + " but received: " + receivedParams.get(i).getDataType());
                        failed = true;
                    }
                }
            } else {
                errorHandler.reportError(SemanticErrorType.FUNCTION_PARAMETERS_NUMBER_INCORRECT, expressionTokens.get(0).getLine(), expressionTokens.get(0).getColumn(), "expected: " + expectedParams.size() + " but received: " + receivedParams.size());
                failed = true;
            }
        } else {
            errorHandler.reportError(SemanticErrorType.FUNCTION_NOT_CALLED_CORRECTLY, expressionTokens.get(0).getLine(), expressionTokens.get(0).getColumn(), expressionTokens.get(0).getLexeme());
            failed = true;
        }
        new ResultContainer(failed, true, null, null, null, "");
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
    private ResultContainer checkValidArithmeticExpression(List<Token> expressionTokens, Symbol<VariableSymbol<?>> variableSymbol, DataType validDatatype, ValueSymbol validValueSymbol) {
        // Check all the tokens are valid for an arithmetic expression (e.g. +, -, *, /, etc.)
        List<TokenType> validArithmeticOperatorsTokens = List.of(MathOperator.SUM, MathOperator.SUB, MathOperator.MUL, MathOperator.DIV);
        List<TokenType> validArithmeticValueTokens = List.of(ValueSymbol.VARIABLE, validValueSymbol);

        List<TokenType> validArithmeticTokens = new ArrayList<>();
        validArithmeticTokens.addAll(validArithmeticValueTokens);
        validArithmeticTokens.addAll(validArithmeticOperatorsTokens);
        boolean failed = false;

        for (Token token : expressionTokens) {
            // Check if the token is inside the valid arithmetic tokens.
            if (!validArithmeticTokens.contains(token.getType())) {
                errorHandler.reportError(SemanticErrorType.INVALID_ARITHMETIC_EXPRESSION, token.getLine(), token.getColumn(), token.getLexeme() + " expected type: " + validDatatype);
                failed = true;
            } else if (token.getType() == ValueSymbol.VARIABLE) {
                // Check if the ID (variable or function) exists and it's a number (integer or float).
                ResultContainer resultContainer = checkVariableSameType(token, List.of(validDatatype));
                ResultContainer resultContainer1 = checkVariableExists(token);
                handleResultContainer(resultContainer1);
                handleResultContainer(resultContainer);
                if (resultContainer.isError() || resultContainer1.isError()) failed = true;
            }

            // Check the operation (sum, sub...) is done between same type of variables / values.
            if (validArithmeticOperatorsTokens.contains(token.getType())) {
                // Check if the previous and next tokens are compatible.
                int tokenIndex = expressionTokens.indexOf(token);
                Token previousToken = expressionTokens.get(tokenIndex - 1);
                Token nextToken = expressionTokens.get(tokenIndex + 1);

                DataType leftOperandType = null;
                DataType rightOperandType = null;

                try {
                    leftOperandType = getOperandDataType(previousToken);
                } catch (IllegalArgumentException e) {
                    failed = true;
                }
                try {
                    rightOperandType = getOperandDataType(nextToken);
                } catch (IllegalArgumentException e) {
                    failed = true;
                }

                if (!failed) {
                    // Check if both operands have same type.
                    if (leftOperandType != rightOperandType) {
                        errorHandler.reportError(SemanticErrorType.INCOMPATIBLE_TYPES, token.getLine(), token.getColumn(), previousToken.getLexeme() + " " + nextToken.getLexeme());
                        failed = true;
                    }
                    // Check if the operand has the same type as the variable.
                    else if (leftOperandType != variableSymbol.getDataType()) {
                        errorHandler.reportError(SemanticErrorType.INCOMPATIBLE_TYPES, token.getLine(), token.getColumn(), leftOperandType + " " + variableSymbol);
                        failed = true;
                    }
                }
            }
        }

        return new ResultContainer(failed, true, null, null, null, "");
    }

    /**
     * Method to determine the data type of token.
     *
     * @param token the token to check.
     * @return the data type of the token.
     * @throws IllegalArgumentException if the token is not a variable or a value.
     */
    private DataType getOperandDataType(Token token) throws IllegalArgumentException {
        if (token.getType() == ValueSymbol.VARIABLE && checkTokenIsVariable(token) && !checkVariableExists(token).isError()) {
            Symbol<?> symbol = getSymbolByLexeme(token.getLexeme());
            return symbol.getDataType();
        } else if (token.getType() instanceof ValueSymbol) {
            return ((ValueSymbol) token.getType()).getDataType();
        } else {
            errorHandler.reportError(SemanticErrorType.INVALID_ARITHMETIC_EXPRESSION, token.getLine(), token.getColumn(), token.getLexeme() + " Unexpected token in arithmetic expression");
            throw new IllegalArgumentException("Unexpected token in arithmetic expression");
        }
    }

    private Symbol<?> getSymbolByLexeme(String lexeme) {
        return symbolTable.findSymbol(lexeme);
    }

    private ResultContainer checkVariableExists(Token token) {
        // Check if the variable exists.
        Symbol<?> symbol = getSymbolByLexeme(token.getLexeme());
        if (symbol == null || symbol.isFunction()) {
            return new ResultContainer(true, false, SemanticErrorType.VARIABLE_NOT_DECLARED, token.getLine(), token.getColumn(), token.getLexeme());
        }
        return new ResultContainer(false, true, null, null, null, "");
    }

    /**
     * Function to check if a given token is a variable.
     *
     * @param token the token to check.
     * @return true if the token is a variable; false otherwise.
     */
    private boolean checkTokenIsVariable(Token token) {
        ResultContainer resultContainer = checkVariableExists(token);

        if (resultContainer.isError()) return false;
        Symbol<?> symbol = getSymbolByLexeme(token.getLexeme());

        boolean isVariable = true;
        if (symbol != null && symbol.isFunction()) {
            isVariable = false;
            errorHandler.reportError(SemanticErrorType.ALREADY_USED_IDENTIFIER, token.getLine(), token.getColumn(), token.getLexeme());
        }
        return isVariable;
    }


    /**
     * Function to check if given expression is a valid boolean expression. It checks if there is a combination where the
     * tokens form a valid logical or relational expression.
     *
     * @param expressionTokens the tokens of the expression.
     * @return a ResultContainer with the result of the check.
     */
    private ResultContainer checkValidBooleanExpression(List<Token> expressionTokens) {
        List<ResultContainer> logicalExpressionEvals = checkLogicalExpression(expressionTokens);
        List<ResultContainer> relationalExpressionEvals = checkRelationalExpression(expressionTokens);
        if (resultsContainNoErrors(logicalExpressionEvals) || resultsContainNoErrors(relationalExpressionEvals)) {
            return new ResultContainer(false, true, null, null, null, "");
        } else {
            if (relationalExpressionEvals.size() < logicalExpressionEvals.size()) {
                for (ResultContainer relationalExpressionEval : relationalExpressionEvals) {
                    handleResultContainer(relationalExpressionEval);
                }
            } else {
                for (ResultContainer logicalExpressionEval : logicalExpressionEvals) {
                    handleResultContainer(logicalExpressionEval);
                }
            }
            return new ResultContainer(true, true, null, null, null, "");
        }
    }

    /**
     * Function to validate logical and relational tokens within an expression.
     * This function checks if the tokens in the given expression are valid based on the provided lists of valid tokens
     * and data types. If a token is invalid, it generates a corresponding ResultContainer object where the error is stored.
     * The list of ResultContainer objects is then returned, and has to be checked for errors in the calling function.
     *
     * @param expressionTokens A list of tokens representing the expression to be validated.
     * @param validTokens      A list of token types that are considered valid for logical and relational operations.
     * @param validDataTypes   A list of data types that are considered valid for the variables within the expression.
     * @return A list of ResultContainer objects, each containing the result of the validation for each token.
     */
    private List<ResultContainer> validateLogicalRelationalTokens(List<Token> expressionTokens, List<TokenType> validTokens, List<DataType> validDataTypes) {
        List<ResultContainer> resultContainers = new ArrayList<>();
        ResultContainer resultContainer;
        for (Token token : expressionTokens) {
            // Check if the token is valid (it is inside the list of "validTokens" which is filled previously).
            if (!validTokens.contains(token.getType())) {
                resultContainer = new ResultContainer(true, false, SemanticErrorType.OPERATOR_NOT_SUPPORTED, token.getLine(), token.getColumn(), token.getLexeme());
                resultContainers.add(resultContainer);
            } else if (token.getType() == ValueSymbol.VARIABLE) {
                // Check if the ID (variable or function) exists and it's a boolean.
                resultContainer = checkVariableSameType(token, validDataTypes);
                ResultContainer resultContainer1 = checkVariableExists(token);
                resultContainers.add(resultContainer1);
                resultContainers.add(resultContainer);
            }
        }

        return resultContainers;
    }

    /**
     * Function to check relational expressions for validity across various data types.
     * This function validates the tokens in a relational expression for different data types, including boolean, integer,
     * float, and char. It uses specific valid tokens for each data type and returns the result with the fewest errors.
     *
     * @param relationalTokens A list of tokens representing the relational expression to be validated.
     * @return A list of ResultContainer objects containing the validation results, prioritizing the result with the fewest errors.
     */
    private List<ResultContainer> checkRelationalExpression(List<Token> relationalTokens) {
        // Check boolean relational expressions
        List<TokenType> validBooleanRelationalTokens = List.of(ValueSymbol.VALUE_TRUE, ValueSymbol.VALUE_FALSE, ValueSymbol.VARIABLE, BinaryOperator.EQ, BinaryOperator.NEQ);
        List<ResultContainer> booleanResultContainers = validateLogicalRelationalTokens(relationalTokens, validBooleanRelationalTokens, List.of(DataType.BOOLEAN));

        // Check integer relational expressions
        List<TokenType> validIntegerRelationalTokens = List.of(ValueSymbol.VALUE_INT, ValueSymbol.VARIABLE, BinaryOperator.GT, BinaryOperator.LT, BinaryOperator.EQ, BinaryOperator.NEQ);
        List<ResultContainer> integerResultContainers = validateLogicalRelationalTokens(relationalTokens, validIntegerRelationalTokens, List.of(DataType.INTEGER));

        // Check float relational expressions
        List<TokenType> validFloatRelationalTokens = List.of(ValueSymbol.VALUE_FLOAT, ValueSymbol.VARIABLE, BinaryOperator.GT, BinaryOperator.LT, BinaryOperator.EQ, BinaryOperator.NEQ);
        List<ResultContainer> floatResultContainers = validateLogicalRelationalTokens(relationalTokens, validFloatRelationalTokens, List.of(DataType.FLOAT));

        // Check char relational expressions
        List<TokenType> validCharRelationalTokens = List.of(ValueSymbol.VALUE_CHAR, ValueSymbol.VARIABLE, BinaryOperator.GT, BinaryOperator.LT, BinaryOperator.EQ, BinaryOperator.NEQ);
        List<ResultContainer> charResultContainers = validateLogicalRelationalTokens(relationalTokens, validCharRelationalTokens, List.of(DataType.CHAR));

        return getLowestErrorsList(List.of(booleanResultContainers, integerResultContainers, floatResultContainers, charResultContainers));
    }

    /**
     * Function to find and return the list of ResultContainer objects with the fewest errors.
     * This function iterates through multiple lists of ResultContainer objects, counts the errors in each list,
     * and returns the list with the lowest number of errors. If a list has no errors, it is returned immediately.
     *
     * @param resultContainers A list of lists of ResultContainer objects, each representing validation results for different expressions.
     * @return The list of ResultContainer objects with the fewest errors. If multiple lists have the same number of errors, the first one encountered is returned.
     */
    private List<ResultContainer> getLowestErrorsList(List<List<ResultContainer>> resultContainers) {
        List<ResultContainer> lowestErrorsList = List.of();
        int lowestErrorCount = Integer.MAX_VALUE;
        for (List<ResultContainer> resultContainer : resultContainers) {
            int errorCount = 0;
            for (ResultContainer container : resultContainer) {
                if (container.isError()) {
                    errorCount++;
                }
            }
            if (errorCount == 0) {
                return resultContainer;
            } else {
                if (errorCount < lowestErrorCount) {
                    lowestErrorCount = errorCount;
                    lowestErrorsList = resultContainer;
                }

            }
        }
        return lowestErrorsList;
    }

    /**
     * Function to check logical expressions for validity.
     *
     * @param logicalTokens A list of tokens representing the logical expression to be validated.
     * @return A list of ResultContainer objects containing the validation results for the logical expression.
     */
        private List<ResultContainer> checkLogicalExpression(List<Token> logicalTokens) {
        // Check all the tokens are valid for a boolean expression (e.g. AND, OR, NOT, etc.)
        List<TokenType> validLogicalTokens = List.of(ValueSymbol.VALUE_TRUE, ValueSymbol.VALUE_FALSE, ValueSymbol.VARIABLE, BinaryOperator.OR, BinaryOperator.AND, BinaryOperator.NOT);
        return validateLogicalRelationalTokens(logicalTokens, validLogicalTokens, List.of(DataType.BOOLEAN));
    }

    /**
     * Function to check if a token is the same type as the given data types in a list.
     *
     * @param token     the token to check.
     * @param dataTypes the list of data types to check against.
     * @return a ResultContainer with the result of the check.
     */
    private ResultContainer checkVariableSameType(Token token, List<DataType> dataTypes) {
        // Check if the ID (variable or function) exists.
        Symbol<?> symbol = getSymbolByLexeme(token.getLexeme());
        ResultContainer resultContainer = new ResultContainer(false, true, null, null, null, "");
        if (symbol != null && !dataTypes.contains(symbol.getDataType())) {
            resultContainer = new ResultContainer(true, false, SemanticErrorType.INCOMPATIBLE_TYPES, token.getLine(), token.getColumn(), "Expected: " + dataTypes + " but received: " + token.getLexeme() + " that is of type: " + symbol.getDataType());
        }
        return resultContainer;
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
        VariableSymbol<Integer> variableSymbol = new VariableSymbol<>(variable.getLexeme(), dataType, variableDatatype.getLine(), false, Integer.class);

        List<Token> assignationTokens = declarationTokens.subList(1, declarationTokens.size());    // Skip DATA_TYPE token.
        // Check the assignments of the variable and add it to the table.
        checkAssignationSemantics(assignationTokens, variableSymbol);

        symbolTable.addSymbol(variableSymbol);
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
                    return;
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

    /**
     * Function to check the semantics of a function declaration.
     * This function performs several checks on the tokens representing a function declaration: it validates the uniqueness
     * of parameter names, checks if the function is already defined, and adds the function along with its parameters to the symbol table.
     * If errors are found during these checks, they are reported via an error handler.
     *
     * @param tokens A list of tokens representing the function declaration to be checked.
     */
    private void checkFunctionDeclarationSemantics(List<Token> tokens) {
        //Obtain the parameters of the function
        int indexOfFirstSeparator = getIndexOfFirstSeparator(tokens, SpecialSymbol.CT);
        List<Token> functionDeclarationTokens = tokens.subList(0, indexOfFirstSeparator);
        List<VariableSymbol<?>> functionParameters = getFunctionDeclarationParameters(functionDeclarationTokens);

        //Check if the parameters have unique names
        ResultContainer resultContainer = checkUniqueVariableNames(functionParameters);
        handleResultContainer(resultContainer);
        //Check if the function is already defined
        boolean functionExists = checkIfFunctionExists(tokens.get(2));
        if (functionExists) {
            errorHandler.reportError(SemanticErrorType.FUNCTION_ALREADY_DEFINED, tokens.get(2).getLine(), tokens.get(2).getColumn(), tokens.get(2).getLexeme());
        }

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

    /**
     * Checks for a list of variables if they have unique names between themselves.
     *
     * @param variables the list of variables to check against.
     * @return a ResultContainer object containing the result of the check.
     */
    private ResultContainer checkUniqueVariableNames(List<VariableSymbol<?>> variables) {
        for (int i = 0; i < variables.size(); i++) {
            for (int j = i + 1; j < variables.size(); j++) {
                if (variables.get(i).getName().equals(variables.get(j).getName())) {
                    return new ResultContainer(true, false, SemanticErrorType.VARIABLE_ALREADY_DEFINED, (int) variables.get(i).getLineDeclaration(), null, variables.get(i).getName());
                }
            }
        }
        return new ResultContainer(false, true, null, null, null, "");
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
        ResultContainer resultContainer = checkValidBooleanExpression(expressionTokens);
        handleResultContainer(resultContainer);
    }

    /**
     * Function to check the semantics of a 'for' loop declaration.
     * This function validates the semantics of tokens representing a 'for' loop. It checks the initial declaration or assignment,
     * ensures that the limit value is of a compatible type, and validates the assignment part of the loop. Errors are reported
     * through the error handler if any semantic issues are detected.
     *
     * @param forTokens A list of tokens representing the 'for' loop to be checked.
     * @param tree      The abstract syntax tree representing the structure of the code.
     */
    public void checkForSemantics(List<Token> forTokens, Tree<AbstractSymbol> tree) {
        // Expected format: FOR (<declaration> TO <literal_num>, <assignation> ) {}

        int indexLastTokenInCondition = getIndexOfFirstSeparator(forTokens, ReservedSymbol.TO);
        List<Token> declarationTokens = forTokens.subList(2, indexLastTokenInCondition);

        // Check if it's an assignment or a declaration
        Tree<AbstractSymbol> abstractSymbolTree = tree.getChildren().get(2);

        String firstTokenName = abstractSymbolTree.getChildren().get(0).getNode().getName();

        // The type of the variable declared in the for loop.
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
            Symbol<?> variable = symbolTable.findSymbolGlobally(declarationTokens.get(0).getLexeme());
            if (variable == null) {
                errorHandler.reportError(SemanticErrorType.VARIABLE_NOT_DECLARED, declarationTokens.get(0).getLine(), declarationTokens.get(0).getColumn(), declarationTokens.get(0).getLexeme());
            } else {
                declaredType = variable.getDataType();
                ResultContainer container = checkAssignationSemantics(declarationTokens, null);
                if (container.isError()) {
                    errorHandler.reportError(container.errorType(), container.optionalLine(), container.optionalColumn(), container.word());
                }
            }
        }

        Token limitValue = forTokens.get(indexLastTokenInCondition + 1);

        TokenType limitValueType = limitValue.getType();
        if (limitValueType == ValueSymbol.VARIABLE) {
            ResultContainer container = checkVariableExists(limitValue);
            if (!container.isError()) {           // Variable exists
                ResultContainer result_container = new ResultContainer(false, true, null, null, null, "");
                if (declaredType != null) {
                    result_container = checkVariableSameType(limitValue, List.of(declaredType));
                }
                if (result_container.isError()) {
                    errorHandler.reportError(SemanticErrorType.INCOMPATIBLE_TYPES, limitValue.getLine(), limitValue.getColumn(), limitValue.getLexeme());
                }
            } else {                        // Variable doesn't exist
                handleResultContainer(container);
            }
        } else {
            // Check if the numeric value is the same type as the variable declared.
            if (declaredType == null || !declaredType.isValidType((ValueSymbol) limitValueType)) {
                errorHandler.reportError(SemanticErrorType.INCOMPATIBLE_TYPES, limitValue.getLine(), limitValue.getColumn(), "Expected type " + declaredType + " but " + limitValue.getLexeme() + " is of type " + limitValueType);
            }
        }

        // Check if the numeric value is the same type as the variable declared.

        int indexLastTokenUntilSeparator = getIndexOfFirstSeparator(forTokens, SpecialSymbol.PT);
        List<Token> assignationTokens = forTokens.subList(indexLastTokenInCondition + 3, indexLastTokenUntilSeparator);
        ResultContainer resultContainer = checkAssignationSemantics(assignationTokens, null);
        handleResultContainer(resultContainer);
    }

    /**
     * Function to find the index of the first occurrence of a specified separator token in a list of tokens.
     * This function iterates through the provided list of tokens and returns the index of the first token that matches
     * the specified separator type. If the separator is not found, it returns the size of the token list.
     *
     * @param tokenList A list of tokens to be searched.
     * @param separator The token type to be identified as the separator.
     * @return The index of the first token in the list that matches the separator type, or the size of the list if the separator is not found.
     */
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

    /**
     * Function to extract function declaration parameters from a list of tokens.
     * This function parses the tokens within parentheses to identify and create variable symbols representing the parameters
     * of a function. It assumes that the tokens follow a specific format where each parameter is declared with its type followed by its name.
     *
     * @param tokens A list of tokens representing the function declaration.
     * @return A list of VariableSymbol objects representing the parameters of the function.
     */
    private List<VariableSymbol<?>> getFunctionDeclarationParameters(List<Token> tokens) {
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
                        if (token.getType() == ValueSymbol.VARIABLE) {
                            String name = token.getLexeme();
                            long lineDeclaration = token.getLine();
                            DataType dt = (DataType) tokens.get(i - 1).getType();

                            parameters.add(new VariableSymbol<>(name, dt, lineDeclaration, true, null));
                        }
                    }
            }
        }
        return parameters;
    }

    /**
     * Function to extract parameters from a function call represented by a list of tokens.
     * This function parses the tokens within parentheses to identify variables passed as parameters
     * in a function call. It checks if each variable exists and retrieves its data type, reporting errors
     * if any variables are not found.
     *
     * @param tokens A list of tokens representing the function call.
     * @return A list of VariableSymbol objects representing the parameters of the function call.
     */
    private List<VariableSymbol<?>> getFunctionCallParameters(List<Token> tokens) {
        List<VariableSymbol<?>> parameters = new ArrayList<>();
        boolean startParams = false;

        for (Token token : tokens) {
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
                        if (token.getType() == ValueSymbol.VARIABLE) {
                            String name = token.getLexeme();
                            long lineDeclaration = token.getLine();
                            ResultContainer resultContainer = checkVariableExists(token);
                            DataType dt = null;
                            if (resultContainer.isError()) {
                                errorHandler.reportError(resultContainer.errorType(), resultContainer.optionalLine(), resultContainer.optionalColumn(), resultContainer.word());
                            } else {
                                dt = getSymbolByLexeme(name).getDataType();
                            }
                            parameters.add(new VariableSymbol<>(name, dt, lineDeclaration, true, null));
                        } else {
                            DataType dt = ((ValueSymbol) token.getType()).getDataType();
                            parameters.add(new VariableSymbol<>(token.getLexeme(), dt, token.getLine(), true, null));
                        }
                    }
                    break;
            }
        }
        return parameters;
    }

    /**
     * Checks if a function has already been declared in the symbol table.
     *
     * @param token the token of the function to check.
     * @return true if the function exists, false otherwise.
     */
    private boolean checkIfFunctionExists(Token token) {
        Symbol<?> symbol = symbolTable.findSymbolGlobally(token.getLexeme());
        return (symbol != null && symbol.isFunction());
    }

    /**
     * Logs the error in the error handler if the result container is an error, and it contains a non-empty message.
     *
     * @param resultContainer the result container to check.
     */
    private void handleResultContainer(ResultContainer resultContainer) {
        if (resultContainer.isError() && !resultContainer.isEmpty()) {
            errorHandler.reportError(resultContainer.errorType(), resultContainer.optionalLine(), resultContainer.optionalColumn(), resultContainer.word());
        }
    }

    /**
     * Checks if a list of result containers contain any errors.
     *
     * @param resultContainers the list of result containers to check.
     * @return true if there are no errors in the list of result containers, false otherwise.
     */
    private boolean resultsContainNoErrors(List<ResultContainer> resultContainers) {
        for (ResultContainer resultContainer : resultContainers) {
            if (resultContainer.isError()) {
                return false;
            }
        }
        return true;
    }
}