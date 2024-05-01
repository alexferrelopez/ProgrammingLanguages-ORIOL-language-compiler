package frontend.lexic.dictionary.tokenEnums;

import frontend.lexic.dictionary.TokenType;

public enum ValueSymbol implements TokenType {
	// Enum constants (mathematics operators)
	VARIABLE("[a-zA-Z_][a-zA-Z0-9_]*"),
	VALUE_INT("-?" + "[0-9]" + "+"),								// -?Dígit+
	VALUE_FLOAT("-?" + "[0-9]" + "(\\.)" + "[0-9]" + "+"),		// -?Dígit+(\.)Dígit+
	VALUE_TRUE("alive"),
	VALUE_FALSE("dead"),
	VALUE_CHAR("'(" + "[a-zA-Z.,_]|\\s" + ")'"),					// ‘Caràcter’ - The backslash (\s) needs to be escaped in Java strings.
	VALUE_STRING("(\"|')(" + "[a-zA-Z.,_]|\\s" + ")+(\"|')");	// (“ + ')CaràcterCaràcter+(“ + ')

	// Instance field (regex pattern) for each enum constant
	private final String regexPattern;

	// Constructor to initialize the instance field (allow it to have a string value)
	ValueSymbol(String pattern) {
		this.regexPattern = pattern;
	}

	// Getter method for the pattern
	@Override
	public String getPattern() {
		return this.regexPattern;
	}
}
