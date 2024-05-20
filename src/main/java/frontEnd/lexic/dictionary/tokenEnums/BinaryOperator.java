package frontEnd.lexic.dictionary.tokenEnums;

import frontEnd.lexic.dictionary.TokenType;

import java.util.List;

public enum BinaryOperator implements TokenType {
    // Enum constants (binary operators)
    GT("bigger|bg", List.of("bigger", "bg")),
    LT("smaller|sm", List.of("smaller", "sm")),
    NEQ("different|diff", List.of("different", "diff")),
    EQ("equals|equ|eq", List.of("equals", "equ", "eq")),
    OR("OR|or", List.of("OR", "or")),
    AND("AND|and", List.of("AND", "and")),
    NOT("NOT|not", List.of("NOT", "not"));

    // Instance field (regex pattern) for each enum constant
    private final String regexPattern;
    private final List<String> translation;

    // Constructor to initialize the instance field (allow it to have a string value)
    BinaryOperator(String pattern, List<String> translation) {
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
