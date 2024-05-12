package frontEnd.semantics;

import frontEnd.exceptions.SemanticException;
import frontEnd.sintaxis.Tree;
import frontEnd.sintaxis.grammar.AbstractSymbol;

public interface SemanticAnalyzerInterface {

	/**
	 * Receive the syntactic tree (from the parser) to analyze it semantically.
	 * @param tree	Parsing tree (syntactic) to analyze.
	 */
	void receiveSyntacticTree(Tree<AbstractSymbol> tree) throws SemanticException;
}
