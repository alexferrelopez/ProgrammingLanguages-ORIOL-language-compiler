package frontEnd.semantics.symbolTable.symbol;

import frontEnd.lexic.dictionary.tokenEnums.DataType;

import java.util.List;

public class FunctionSymbol<T> extends Symbol<T> {
	private final List<VariableSymbol<?>> parameters;  // List of parameters that act as variables inside the function.
	private int rootChildIndex;	// Index of the root child in the tree (to identify the function's scope).

	public FunctionSymbol(String name, DataType returnType, List<VariableSymbol<?>> parameters, long lineDeclaration, Class<T> typeClass) {
		super(name, returnType, lineDeclaration, typeClass);
		this.parameters = parameters;
	}

	public List<VariableSymbol<?>> getParameters() {
		return parameters;
	}

	/**
	 * Check if a symbol is a variable.
	 *
	 * @return true if the symbol is a variable; false otherwise.
	 */
	@Override
	public boolean isVariable() {
		return false;
	}

	@Override
	public boolean isFunction() {
		return true;
	}

	public void setRootChildIndex(int rootChildIndex) {
		this.rootChildIndex = rootChildIndex;
	}

	public int getRootChildIndex() {
		return rootChildIndex;
	}
}
