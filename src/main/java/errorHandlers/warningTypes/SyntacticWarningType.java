package errorHandlers.warningTypes;

/**
 * Enum for different types of parser/syntactic analyzer warnings
 */
public enum SyntacticWarningType implements WarningType {

    ;

    final String message;

    SyntacticWarningType(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}