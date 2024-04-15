package FrontEnd.Dictionary.TokenEnums;

import FrontEnd.Dictionary.TokenType;

public enum ReservedSymbol implements TokenType {
	// Enum constants (special symbols)
	IF("check"),
	ELSE("else|otherwise"),
	WHILE("breed"),
	FOR("feed"),
	DO("do"),
	TO("to"),
	RETURN("poop"),
	MAIN("ranch"),
	COMMENT("Farmer: " + ValueSymbol.VALUE_STRING.getPattern()),	// Farmer: VALOR_STRING
	AARON("/aaron/i"),
	ORIOL("/oriol/i"),
	ALEXIA("/al(e|è)xia/i"),
	GEMMA("/gemma/i"),
	ALEX("/(a|à)lex/i");

	// Instance field (regex pattern) for each enum constant
	private final String regexPattern;

	// Constructor to initialize the instance field (allow it to have a string value)
	ReservedSymbol(String pattern) {
		this.regexPattern = pattern;
	}

	// Getter method for the pattern
	@Override
	public String getPattern() {
		return this.regexPattern;
	}
}
