package frontEnd.semantics;

import frontEnd.sintaxis.Tree;
import frontEnd.sintaxis.grammar.AbstractSymbol;

public interface SemanticAnalyzerInterface {
	void receiveTree(Tree<AbstractSymbol> tree);
}
