package frontEnd.lexic.dictionary.tokenEnums;

import frontEnd.lexic.dictionary.TokenType;

import java.util.Collections;
import java.util.List;

public enum ValueSymbol implements TokenType {
    // Enum constants (mathematics operators)
    VALUE_INT("-?" + "[0-9]" + "+", Collections.emptyList()),                            // -?Dígit+
    VALUE_FLOAT("-?" + "[0-9]" + "(\\.)" + "[0-9]" + "+", Collections.emptyList()),    // -?Dígit+(\.)Dígit+
    VALUE_TRUE("alive", List.of("alive")),
    VALUE_FALSE("dead", List.of("dead")),
    VALUE_CHAR("'(" + "[a-zA-Z.,_]|\\s" + ")'", Collections.emptyList()),                    // ‘Caràcter’ - The backslash (\s) needs to be escaped in Java strings.
    VALUE_STRING("(\"|')(" + "[a-zA-Z.,_]|\\s" + ")+(\"|')", Collections.emptyList()),    // (“ + ')CaràcterCaràcter+(“ + ')
    VARIABLE("[a-zA-Z_][a-zA-Z0-9_]*", Collections.emptyList());

    // Instance field (regex pattern) for each enum constant
    private final String regexPattern;
    private final List<String> translation;

    // Constructor to initialize the instance field (allow it to have a string value)
    ValueSymbol(String pattern, List<String> translation) {
        this.regexPattern = pattern;
        this.translation = translation;
    }

    public DataType getDataType() {
		return switch (this) {
			case VALUE_INT -> DataType.INTEGER;
			case VALUE_FLOAT -> DataType.FLOAT;
			case VALUE_TRUE, VALUE_FALSE -> DataType.BOOLEAN;
			case VALUE_CHAR -> DataType.CHAR;
			case VALUE_STRING -> DataType.STRING;
			default -> DataType.VOID;
		};
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
