package FrontEnd.Dictionary.TokenEnums;

import FrontEnd.Dictionary.TokenType;

public enum ValueSymbol implements TokenType {
	// Enum constants (mathematics operators)
	DIGIT("[0-9]"),
	VARIABLE("[a-zA-Z_][a-zA-Z0-9_]*"),
	VALUE_INT("-?" + DIGIT.getPattern() + "+"),									// -?Dígit+
	VALUE_FLOAT("-?" + DIGIT.getPattern() + "(\\.)" + DIGIT.getPattern() + "+"),	// -?Dígit+(\.)Dígit+
	VALUE_TRUE("alive"),
	VALUE_FALSE("dead"),
	VALUE_CHAR("'(" + "[a-zA-Z.,_]|\\s" + ")'"),									// ‘Caràcter’ - The backslash (\s) needs to be escaped in Java strings.
	VALUE_STRING("(\"|')(" + "[a-zA-Z.,_]|\\s" + ")+(\"|')");					// (“ + ')CaràcterCaràcter+(“ + ')

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
