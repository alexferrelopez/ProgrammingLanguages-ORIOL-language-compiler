package FrontEnd.SymbolTable.SymbolType;

import FrontEnd.Dictionary.TokenEnums.DataType;
import FrontEnd.SymbolTable.Scope;
import FrontEnd.SymbolTable.Symbol;

public class VariableSymbol<T> extends Symbol<T> {
	private final boolean isFunctionParameter;

	public VariableSymbol(String name, DataType dataType, long lineDeclaration, Scope scope, boolean isFunctionParameter) {
		super(name, dataType, lineDeclaration, scope);
		this.isFunctionParameter = isFunctionParameter;
	}

	public boolean isFunctionParameter() {
		return this.isFunctionParameter;
	}
}
