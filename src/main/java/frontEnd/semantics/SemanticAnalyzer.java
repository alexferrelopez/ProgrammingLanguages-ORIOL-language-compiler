package frontEnd.semantics;

import debug.PrettyPrintTree;
import errorHandlers.SemanticErrorHandler;
import frontEnd.lexic.dictionary.Token;
import frontEnd.lexic.dictionary.TokenType;
import frontEnd.lexic.dictionary.tokenEnums.DataType;
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
                break;
            case "assignation":
                //checkAssignationSemantics(tokens);
                break;
            case "func_type":
                printTree(tree);
                checkFunctionDeclarationSemantics(tokens);
                break;
            case "CT":
                symbolTable.leaveCurrentScope();
                break;
            // ...
        }
    }

    private void checkFunctionDeclarationSemantics(List<Token> tokens) {

        String name = tokens.get(2).getLexeme();
        DataType dataType = (DataType) tokens.get(0).getType();
        long lineDeclaration = tokens.get(0).getLine();

        List<VariableSymbol<?>> variables = getFunctionParameters(tokens);

        FunctionSymbol functionSymbol = null;
        checkIfFunctionExists(tokens.get(2));
        symbolTable.addSymbol(functionSymbol);
        symbolTable.addScope(ScopeType.FUNCTION);
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

    private void checkIfFunctionExists(Token token) {
        Map<String, Symbol<?>> x = symbolTable.getCurrentScope().getSymbols();
        System.out.println();
    }




    private boolean findReturn(Tree tree) {

        Tree tree2 = (Tree) ((Tree) tree.getChildren().get(0)).getChildren().get(0);
        TerminalSymbol ts = (TerminalSymbol) tree2.getNode();
        Token token = ts.getToken();
        printTree(tree2);
        Symbol<?> symbol = symbolTable.findSymbol(token.getLexeme());

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

}
