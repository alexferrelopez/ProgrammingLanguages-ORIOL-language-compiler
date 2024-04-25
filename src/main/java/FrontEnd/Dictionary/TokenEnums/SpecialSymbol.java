package FrontEnd.Dictionary.TokenEnums;

import FrontEnd.Dictionary.TokenType;

import java.util.List;

public enum SpecialSymbol implements TokenType {
	// Enum constants (special symbols)
	PO("\\(", List.of("(")),
	PT("\\)", List.of(")")),
	COMMA(",", List.of(",")),
	PUNT_COMMA(";", List.of(";")),
	CO("\\{" , List.of("{")),
	CT("\\}", List.of("}")),
	DOS_PUNTS(":", List.of(":")),
	BRACKET_O("\\[" , List.of("[")),
	BRACKET_C("\\]", List.of("]")),
	IS("is", List.of("is"));

	// Instance field (regex pattern) for each enum constant
	private final String regexPattern;
	private final List<String> translation;

	// Constructor to initialize the instance field (allow it to have a string value)
	SpecialSymbol(String pattern, List<String> translation) {
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
