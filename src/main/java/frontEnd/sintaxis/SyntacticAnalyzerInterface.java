package frontEnd.sintaxis;

import frontEnd.sintaxis.grammar.AbstractSymbol;

public interface SyntacticAnalyzerInterface {

    /**
     * This method starts the lexical, syntactic and semantic analysis of the code.
     */
    void parseProgram();

    Tree<AbstractSymbol> getTree();

    void printTree(Tree<AbstractSymbol> tree);
}
