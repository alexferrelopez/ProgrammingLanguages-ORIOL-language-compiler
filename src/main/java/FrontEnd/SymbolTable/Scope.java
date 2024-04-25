package FrontEnd.SymbolTable;

public enum Scope {
	GLOBAL,			// Outside any function definitions.
	FUNCTION,		// Inside any function definitions (including the function parameters).
	CONDITIONAL,	// Inside any if-else statement.
	LOOP			// Inside any for-while statement.
}
