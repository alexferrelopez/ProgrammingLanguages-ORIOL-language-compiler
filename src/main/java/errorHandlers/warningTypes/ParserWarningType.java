package errorHandlers.warningTypes;

/**
 * Enum for different types of parser warnings
 */
public enum ParserWarningType implements WarningType {

    ;
    //TODO: Add different types of warnings

    final String message;

    ParserWarningType(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}