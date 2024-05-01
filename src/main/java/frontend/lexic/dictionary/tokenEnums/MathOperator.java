package frontend.lexic.dictionary.tokenEnums;

import frontend.lexic.dictionary.TokenType;

public enum MathOperator implements TokenType {
	// Enum constants (mathematics operators)
	SUM("sum"),
	SUB("minus"),
	MOD("mod|module"),
	MUL("times"),
	POW("pow|power"),
	DIV("div");

	// Instance field (regex pattern) for each enum constant
	private final String regexPattern;

	// Constructor to initialize the instance field (allow it to have a string value)
	MathOperator(String pattern) {
		this.regexPattern = pattern;
	}

	// Getter method for the pattern
	@Override
	public String getPattern() {
		return this.regexPattern;
	}
}
