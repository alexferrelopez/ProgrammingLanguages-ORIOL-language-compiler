package frontend.Dictionary.TokenEnums;

import frontend.Dictionary.TokenType;

public enum ReservedSymbol implements TokenType {



	// Enum constants (special symbols)
	IF("check"),
	ELSE("else|otherwise"),
	WHILE("breed"),
	FOR("feed"),
	DO("do"),
	TO("to"),
	RETURN("poop"),
	EOF("EOF"),
	MAIN("ranch"),
	EPSILON("ε"),
	COMMENT("Farmer: " + ValueSymbol.VALUE_STRING.getPattern()),    // Farmer: VALOR_STRING
	AARON("(?i)aaron"),                                            // Regex pattern case insensitive by adding (?i)
	ORIOL("(?i)oriol"),
	ALEXIA("(?i)al(e|è)xia"),
	GEMMA("(?i)gemma"),
	ALEX("(?i)(a|à)lex");

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