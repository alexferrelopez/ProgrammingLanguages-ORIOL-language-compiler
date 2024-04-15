package FrontEnd.Dictionary.TokenEnums;

import FrontEnd.Dictionary.TokenType;

public enum DataType implements TokenType {
	// Enum constants (special symbols)
	INTEGER("miau"),
	FLOAT("oink"),
	BOOLEAN("status"),
	CHAR("moo"),
	STRING("quack"),
	VOID("void");

	// Instance field (regex pattern) for each enum constant
	private final String regexPattern;

	// Constructor to initialize the instance field (allow it to have a string value)
	DataType(String pattern) {
		this.regexPattern = pattern;
	}

	// Getter method for the pattern
	@Override
	public String getPattern() {
		return this.regexPattern;
	}
}
