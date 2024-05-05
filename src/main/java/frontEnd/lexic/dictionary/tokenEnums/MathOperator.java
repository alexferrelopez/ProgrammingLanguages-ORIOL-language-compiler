package frontEnd.lexic.dictionary.tokenEnums;

import frontEnd.lexic.dictionary.TokenType;

import java.util.List;

public enum MathOperator implements TokenType {
    // Enum constants (mathematics operators)
    SUM("sum", List.of("sum")),
    SUB("minus", List.of("minus")),
    MOD("mod|module", List.of("mod", "module")),
    MUL("times", List.of("times")),
    POW("pow|power", List.of("pow", "power")),
    DIV("div", List.of("div"));

    // Instance field (regex pattern) for each enum constant
    private final String regexPattern;
    private final List<String> translation;

    // Constructor to initialize the instance field (allow it to have a string value)
    MathOperator(String pattern, List<String> translation) {
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
