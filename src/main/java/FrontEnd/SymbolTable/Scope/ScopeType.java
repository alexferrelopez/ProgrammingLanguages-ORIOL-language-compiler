package FrontEnd.SymbolTable.Scope;

public enum ScopeType {
	GLOBAL,				// Outside any function definitions.
	FUNCTION,			// Inside any function definitions (including the function parameters).
	CONDITIONAL_LOOP,	// Inside any if-else or for-while statement.
}