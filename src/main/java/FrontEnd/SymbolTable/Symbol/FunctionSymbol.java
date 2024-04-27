package FrontEnd.SymbolTable.Symbol;

import FrontEnd.Dictionary.TokenEnums.DataType;
import FrontEnd.SymbolTable.Scope.ScopeType;

import java.util.List;

public class FunctionSymbol<T> extends Symbol<T> {
	private final List<VariableSymbol<?>> parameters;  // List of parameters that act as variables inside the function.

	public FunctionSymbol(String name, DataType returnType, List<VariableSymbol<?>> parameters, long lineDeclaration, ScopeType scopeType) {
		super(name, returnType, lineDeclaration, scopeType);
		this.parameters = parameters;
	}

	public List<VariableSymbol<?>> getParameters() {
		return parameters;
	}
}
