package FrontEnd.Dictionary.TokenEnums;

import FrontEnd.Dictionary.TokenType;

import java.util.List;

public enum ReservedSymbol implements TokenType {
	// Enum constants (special symbols)
	IF("check", List.of("check")),
	ELSE("else|otherwise", List.of("else", "otherwise")),
	WHILE("breed", List.of("breed")),
	FOR("feed", List.of("feed")),
	DO("do", List.of("do")),
	TO("to", List.of("to")),
	RETURN("poop", List.of("poop")),
	EOF("EOF", List.of("EOF")),
	MAIN("ranch", List.of("ranch")),
	COMMENT("Farmer: " + ValueSymbol.VALUE_STRING.getPattern(), List.of("")),	// Farmer: VALOR_STRING
	AARON("(?i)aaron", List.of("aaron")),																		// Regex pattern case insensitive by adding (?i)
	ORIOL("(?i)oriol", List.of("oriol")),
	ALEXIA("(?i)al(e|è)xia", List.of("alexia", "alèxia")),
	GEMMA("(?i)gemma", List.of("gemma")),
	ALEX("(?i)(a|à)lex", List.of("alex", "àlex")),;

	// Instance field (regex pattern) for each enum constant
	private final String regexPattern;
	private final List<String> translation;

	// Constructor to initialize the instance field (allow it to have a string value)
	ReservedSymbol(String pattern, List<String> translation) {
		this.regexPattern = pattern;
		this.translation = translation;
	}

	// Getter method for the pattern
	@Override
	public String getPattern() {
		return this.regexPattern;
	}

	@Override
	public List<String> getTranslation() {
		return this.translation;
	}
}
