package errorHandlers.warningTypes;

/**
 * Enum for different types of semantic warnings
 */
public enum SemanticWarningType implements WarningType {
    ;

    final String message;

    SemanticWarningType(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}