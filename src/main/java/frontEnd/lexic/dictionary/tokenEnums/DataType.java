package frontEnd.lexic.dictionary.tokenEnums;

import frontEnd.lexic.dictionary.TokenType;

import java.util.List;

public enum DataType implements TokenType {
    // Enum constants (special symbols)
    INTEGER("miau", List.of("miau")),
    FLOAT("oink", List.of("oink")),
    BOOLEAN("status", List.of("status")),
    CHAR("moo", List.of("moo")),
    STRING("quack", List.of("quack")),
    VOID("void", List.of("void"));

    // Instance field (regex pattern) for each enum constant
    private final String regexPattern;
    private final List<String> translation;

    // Constructor to initialize the instance field (allow it to have a string value)
    DataType(String pattern, List<String> translation) {
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
