package frontEnd.semantics;

import debug.PrettyPrintTree;
import errorHandlers.SemanticErrorHandler;
import frontEnd.lexic.dictionary.Token;
import frontEnd.semantics.symbolTable.SymbolTableTree;
import frontEnd.semantics.symbolTable.scope.ScopeType;
import frontEnd.semantics.symbolTable.symbol.Symbol;
import frontEnd.sintaxis.Tree;
import frontEnd.sintaxis.grammar.AbstractSymbol;
import frontEnd.sintaxis.grammar.derivationRules.TerminalSymbol;

import java.util.ArrayList;
import java.util.List;

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
        /*
        List<Token> tokens = new ArrayList<>();
        List<AbstractSymbol> terminalSymbols = TreeTraversal.getLeafNodesIterative(tree);
        List<Token> tokens = convertSymbolsIntoTokens(terminalSymbols);
        */

        // Check the first node (root) to see what kind of grammatical operation is done and apply its semantics.
        switch (tree.getNode().toString()) {
            case "declaration":
                break;
            case "assignation":
                //checkAssignationSemantics(tokens);
                break;
            case "func_decl":
                printTree(tree);
                if(isOpenFunction(tree)){
                    symbolTable.addScope(ScopeType.FUNCTION);
                }else{
                    //TODO: Sortir del scope de funció i pujar un nivell
                }
                //findReturn(tree);
                break;
            // ...
        }
    }

    private boolean isOpenFunction(Tree tree) {
        Tree stmtList_funcBody = (Tree)((Tree) ((Tree) ((Tree) tree.getChildren().get(0)).getChildren().get(1)).getChildren().get(1)).getChildren().get(1);
        return stmtList_funcBody.getChildren().isEmpty();

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
}
