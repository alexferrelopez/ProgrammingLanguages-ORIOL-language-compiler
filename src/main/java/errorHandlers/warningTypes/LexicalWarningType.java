package errorHandlers.warningTypes;

/**
 * Enum for different types of lexical warnings
 */
public enum LexicalWarningType implements WarningType {
    //TODO: Add different types of warnings
    VARIABLE_NAME_TOO_LONG_WARNING(""),
    VARIABLE_NAME_TOO_SHORT_WARNING(""),
    ;

    final String message;

    LexicalWarningType(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}