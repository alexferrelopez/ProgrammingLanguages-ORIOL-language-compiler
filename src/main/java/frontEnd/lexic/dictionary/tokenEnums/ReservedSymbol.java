package frontEnd.lexic.dictionary.tokenEnums;

import frontEnd.lexic.dictionary.TokenType;

import java.util.Collections;
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
    EPSILON("ε", Collections.emptyList()),
    COMMENT("Farmer: " + ValueSymbol.VALUE_STRING.getPattern(), List.of("Farmer: \"This is an example\"")),    // Farmer: VALOR_STRING
    IS("is", List.of("is"));

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