package frontEnd.semantics.symbolTable.symbol;

import frontEnd.lexic.dictionary.tokenEnums.DataType;

public class VariableSymbol<T> extends Symbol<T> {
	private final boolean isFunctionParameter;

	public VariableSymbol(String name, DataType dataType, long lineDeclaration, boolean isFunctionParameter, Class<T> typeClass) {
		super(name, dataType, lineDeclaration, typeClass);
		this.isFunctionParameter = isFunctionParameter;
	}

	public boolean isFunctionParameter() {
		return this.isFunctionParameter;
	}

	/**
	 * Check if a symbol is a variable.
	 *
	 * @return true if the symbol is a variable; false otherwise.
	 */
	@Override
	public boolean isVariable() {
		return true;
	}

	@Override
	public boolean isFunction() {
		return false;
	}

}
