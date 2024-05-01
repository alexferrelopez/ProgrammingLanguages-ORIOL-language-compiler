package frontend.lexic.dictionary.tokenEnums;

import frontend.lexic.dictionary.TokenType;

public enum BinaryOperator implements TokenType {
	// Enum constants (binary operators)
	GT("bigger|bg"),
	LT("smaller|sm"),
	NEQ("different|diff"),
	EQ("equals|equ|eq"),
	OR("OR|or"),
	AND("AND|and");

	// Instance field (regex pattern) for each enum constant
	private final String regexPattern;

	// Constructor to initialize the instance field (allow it to have a string value)
	BinaryOperator(String pattern) {
		this.regexPattern = pattern;
	}

	// Getter method for the pattern
	@Override
	public String getPattern() {
		return this.regexPattern;
	}
}
