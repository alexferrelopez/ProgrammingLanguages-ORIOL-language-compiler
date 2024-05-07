package frontEnd.semantics;

import errorHandlers.SemanticErrorHandler;
import frontEnd.lexic.dictionary.Token;
import frontEnd.semantics.symbolTable.SymbolTableTree;
import frontEnd.semantics.symbolTable.symbol.Symbol;
import frontEnd.sintaxis.Tree;
import frontEnd.sintaxis.grammar.AbstractSymbol;

import java.util.ArrayList;
import java.util.List;

import static errorHandlers.errorTypes.SemanticErrorType.DUPLICATE_SYMBOL_DECLARATION;
import static errorHandlers.errorTypes.SemanticErrorType.FUNCTION_NOT_DECLARED;

public class SemanticAnalyzer {
    private final SemanticErrorHandler errorHandler;
    private SymbolTableTree symbolTable;

    public SemanticAnalyzer(SemanticErrorHandler semanticErrorHandler, SymbolTableTree symbolTable) {
        this.errorHandler = semanticErrorHandler;
        this.symbolTable = symbolTable;
    }

    /**
     * Function to check the semantic of the tree received from the parser.
     *
     * @param tree the tree that we receive from the parser.
     */
    public static void sendTree(Tree tree) {
        //TODO implement this method

        // We receive a tree that each node is the type AbstractSymbol

        // We can use a switch statement to check the type of each node
        // We can use the method getType() to get the type of the node
        // Get a list of terminal symbols (tokens with lexical meaning).
        List<Token> tokens = new ArrayList<>();

        // Check the first node (root) to see what kind of grammatical operation is done and apply its semantics.
        switch (tree.getNode().toString()) {
            case "declaration":
                // Call the method to check the declaration of a variable

                break;
            case "assignation":
                break;
            // ...
        }
    }


    /**
     * Function to check if a symbol is declared in the current scope.
     * @param tokens the symbol to check.
     */
    public void checkDeclaration(List<Token> tokens) {
        // example: miau a = 3;
        // We have to check if the variable "a" is declared in the current scope (since current scope to global scope, root)
        if (symbolTable.findSymbol(tokens.get(1).getLexeme()) == null) {
            errorHandler.reportError(DUPLICATE_SYMBOL_DECLARATION, tokens.get(1).getLine(), tokens.get(1).getColumn(), "Variable not declared");
        }
    }

    public void checkFunctionCall(List<Token> tokens) {
        // example: miau(3, 4, 5);

        // We have to check if the function is declared
        // All the functions are stored in the root node
        if (symbolTable.findSymbol(tokens.get(0).getLexeme()) == null) {
            errorHandler.reportError(FUNCTION_NOT_DECLARED, tokens.get(0).getLine(), tokens.get(0).getColumn(), "Function not declared");
        }

        // We have to check if the parameters that the function receives are correct

    }

    // Additional methods for semantic checks can be added here
}

