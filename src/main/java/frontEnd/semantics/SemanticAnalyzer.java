package frontEnd.semantics;

import debug.PrettyPrintTree;
import errorHandlers.SemanticErrorHandler;
import errorHandlers.errorTypes.SemanticErrorType;
import frontEnd.lexic.dictionary.Token;
import frontEnd.lexic.dictionary.TokenType;
import frontEnd.lexic.dictionary.tokenEnums.DataType;
import frontEnd.lexic.dictionary.tokenEnums.ReservedSymbol;
import frontEnd.lexic.dictionary.tokenEnums.ValueSymbol;
import frontEnd.semantics.symbolTable.SymbolTableTree;
import frontEnd.semantics.symbolTable.scope.ScopeNode;
import frontEnd.semantics.symbolTable.scope.ScopeType;
import frontEnd.semantics.symbolTable.symbol.FunctionSymbol;
import frontEnd.semantics.symbolTable.symbol.Symbol;
import frontEnd.semantics.symbolTable.symbol.VariableSymbol;
import frontEnd.sintaxis.Tree;
import frontEnd.sintaxis.grammar.AbstractSymbol;
import frontEnd.sintaxis.grammar.derivationRules.TerminalSymbol;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SemanticAnalyzer {
    private final SemanticErrorHandler errorHandler;
    private final SymbolTableTree symbolTable;
    private boolean returnFound = false;
    private boolean mainFound = false;

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

    public SemanticAnalyzer(SemanticErrorHandler semanticErrorHandler, SymbolTableTree symbolTable) {
        errorHandler = semanticErrorHandler;
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
        List<AbstractSymbol> terminalSymbols = TreeTraversal.getLeafNodesIterative(tree);
        List<Token> tokens = convertSymbolsIntoTokens(terminalSymbols);


        // Check the first node (root) to see what kind of grammatical operation is done and apply its semantics.
        switch (tree.getNode().toString()) {
            case "declaration":
                VariableSymbol variableSymbol = new VariableSymbol(tokens.get(1).getLexeme(), (DataType) tokens.get(0).getType(), 0, false);
                symbolTable.addSymbol(variableSymbol);//TODO: treure aixo, es nomes per veure si funciona
                break;
            case "assignation":
                //checkAssignationSemantics(tokens);
                break;
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
            case "EOF":
                checkEOFSematics();
                break;
            // ...
        }
    }

    /**
     * Check the semantics after finishing
     */
    private void checkEOFSematics() {
        if(!mainFound){
            errorHandler.reportError(SemanticErrorType.MAIN_FUNCTION_MISSING, null, null, SemanticErrorType.MAIN_FUNCTION_MISSING.getMessage());
            printError(SemanticErrorType.MAIN_FUNCTION_MISSING.getMessage());
        }
    }

    /**
     * Check the semantics of a closing bracket
     * @param tokens the tokens of the closing bracket
     */
    private void checkCloseBracketsSemantics(List<Token> tokens) {
        if(symbolTable.getCurrentScope().getScopeType() == ScopeType.FUNCTION){
            if(!returnFound){
                errorHandler.reportError(SemanticErrorType.RETURN_STATEMENT_MISSING, tokens.get(0).getLine(), 0, SemanticErrorType.RETURN_STATEMENT_MISSING.getMessage());
                printError(SemanticErrorType.RETURN_STATEMENT_MISSING.getMessage());
            }
        }
    }

    /**
     * Check the semantics of a return declaration
     * @param tokens the tokens of the return declaration
     */
    private void checkReturnSemantics(List<Token> tokens) {
        Token token = tokens.get(1);
        if(token.getLexeme().equals("void")){
            DataType functionReturnType = symbolTable.getCurrentScope().getReturnType();
            if(functionReturnType != DataType.VOID){
                errorHandler.reportError(SemanticErrorType.FUNCTION_RETURN_TYPE_NOT_CORRECT, token.getLine(), 0, "Return type is not correct expected " + functionReturnType + " but received void");
                printError("Return type is not correct expected " + functionReturnType + " but received void");
            }
        }else{
            ValueSymbol type = (ValueSymbol) token.getType();
            DataType returnType = null;
            if(type == ValueSymbol.VARIABLE){
                Symbol<?> symbol = symbolTable.findSymbol(token.getLexeme());
                if (Objects.isNull(symbol)) {
                    errorHandler.reportError(SemanticErrorType.VARIABLE_NOT_DECLARED, token.getLine(), 0, "Variable " + token.getLexeme() + " not declared");
                    printError("Variable " + token.getLexeme() + " not declared");
                }
                returnType =  symbol.getDataType();
            }else{
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
            if(returnType != functionReturnType){
                errorHandler.reportError(SemanticErrorType.FUNCTION_RETURN_TYPE_NOT_CORRECT, token.getLine(), 0, "Return type is not correct expected " + functionReturnType + " but received " + returnType);
                printError("Return type is not correct expected " + functionReturnType + " but received " + returnType);
            }
        }
        if(returnFound){
            errorHandler.reportError(SemanticErrorType.RETURN_SECOND, token.getLine(), 0, SemanticErrorType.RETURN_SECOND.getMessage());
            printError(SemanticErrorType.RETURN_SECOND.getMessage());
        }
    }


    private void checkFunctionDeclarationSemantics(List<Token> tokens) {

        //Obtain the parameters of the function
        List<VariableSymbol<?>> functionParameters = getFunctionParameters(tokens);

        //Check if the parameters have unique names
        if(checkUniquevariableNames(functionParameters)){
            //return; TODO
        }
        //Check if the function is already defined
        if(checkIfFunctionExists(tokens.get(2))){
            //return; TODO
        }

        //Obtain information about the function
        String functionName = tokens.get(2).getLexeme();
        DataType dataTypeReturn = (DataType) tokens.get(0).getType();
        long lineDeclaration = tokens.get(0).getLine();

        FunctionSymbol functionSymbol = new FunctionSymbol(functionName, dataTypeReturn, functionParameters, lineDeclaration);

        symbolTable.addSymbol(functionSymbol);
        symbolTable.addScope(ScopeType.FUNCTION, dataTypeReturn);

        //Add the parameters to the symbol table
        for(VariableSymbol<?> parameter: functionParameters){
            symbolTable.addSymbol(parameter);
        }

        checkMainFunction(tokens.get(2));

    }

    private void checkMainFunction(Token token) {
        if(token.getType() == ReservedSymbol.MAIN){
            if(mainFound){
                errorHandler.reportError(SemanticErrorType.MAIN_FUNCTION_ALREADY_DEFINED, token.getLine(), 0, SemanticErrorType.MAIN_FUNCTION_ALREADY_DEFINED.getMessage());
                printError(SemanticErrorType.MAIN_FUNCTION_ALREADY_DEFINED.getMessage());
            }else{
                mainFound = true;
            }
        }
    }

    private boolean checkUniquevariableNames(List<VariableSymbol<?>> variables) {
        for(int i = 0; i < variables.size(); i++){
            for(int j = i+1; j < variables.size(); j++){
                if(variables.get(i).getName().equals(variables.get(j).getName())){
                    errorHandler.reportError(SemanticErrorType.VARIABLE_ALREADY_DEFINED, (int)variables.get(i).getLineDeclaration(), 0, "Variable " + variables.get(i).getName() + " is already defined");
                    printError("Variable " + variables.get(i).getName() + " is already defined");
                    return true;
                }
            }
        }

        return false;
    }

    private List<VariableSymbol<?>> getFunctionParameters(List<Token> tokens) {
        List<VariableSymbol<?>> parameters = new ArrayList<>();
        boolean startParams = false;
        int i = 0;
        for(Token token: tokens){
            if(Objects.isNull(token)) continue;
            switch (token.getLexeme()){
                case "(" :
                    startParams = true;
                    break;
                case ")" :
                    startParams = false;
                    break;
                case "," :
                    break;
                default:
                    if(startParams){
                        TokenType tt = token.getType();
                        if(token.getType().toString().equals("VARIABLE")){
                            String name = token.getLexeme();
                            long lineDeclaration = token.getLine();
                            DataType dt = (DataType) tokens.get(i-1).getType();
                            parameters.add(new VariableSymbol<>(name, dt, lineDeclaration, true));
                        }

                        System.out.println(tt.toString());
                    }
            }
            i++;
        }
        return parameters;
    }

    private boolean checkIfFunctionExists(Token token) {
        Map<String, Symbol<?>> x = symbolTable.getCurrentScope().getSymbols();
        if(x.containsKey(token.getLexeme())){
            errorHandler.reportError(SemanticErrorType.FUNCTION_ALREADY_DEFINED, token.getLine(), token.getColumn(), SemanticErrorType.FUNCTION_ALREADY_DEFINED.getMessage());
            printError(SemanticErrorType.FUNCTION_ALREADY_DEFINED.getMessage());
            return true;
        }
        return false;
    }




    private void printTree(Tree<AbstractSymbol> tree) {
        PrettyPrintTree<Tree<AbstractSymbol>> printTree = new PrettyPrintTree<>(
                Tree::getChildren,
                Tree::getNode
        );

        printTree.display(tree);
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

    private void printError(String error){
        System.out.println("\u001B[31m"+  error + "\u001B[0m");
    }

}
