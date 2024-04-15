package FrontEnd.Dictionary.TokenEnums;

import FrontEnd.Dictionary.TokenType;

public enum SpecialSymbol implements TokenType {
	// Enum constants (special symbols)
	PO("("),
	PT(")"),
	COMMA(","),
	PUNT_COMMA(";"),
	CO("{"),
	CT("}"),
	DOS_PUNTS(":"),
	BRACKET_O("["),
	BRACKET_C("]"),
	IS("is");

	// Instance field (regex pattern) for each enum constant
	private final String regexPattern;

	// Constructor to initialize the instance field (allow it to have a string value)
	SpecialSymbol(String pattern) {
		this.regexPattern = pattern;
	}

	// Getter method for the pattern
	@Override
	public String getPattern() {
		return this.regexPattern;
	}
}
