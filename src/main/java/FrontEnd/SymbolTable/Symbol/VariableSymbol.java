package FrontEnd.SymbolTable.Symbol;

import FrontEnd.Dictionary.TokenEnums.DataType;

public class VariableSymbol<T> extends Symbol<T> {
	private final boolean isFunctionParameter;

	public VariableSymbol(String name, DataType dataType, long lineDeclaration, boolean isFunctionParameter) {
		super(name, dataType, lineDeclaration);
		this.isFunctionParameter = isFunctionParameter;
	}

	public boolean isFunctionParameter() {
		return this.isFunctionParameter;
	}
}
