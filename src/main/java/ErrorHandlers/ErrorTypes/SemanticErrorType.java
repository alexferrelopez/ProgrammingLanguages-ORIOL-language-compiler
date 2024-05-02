package ErrorHandlers.ErrorTypes;

/**
 * Enum for semantic errors
 */
public enum SemanticErrorType implements ErrorType {
    ;

    //TODO: Add different types of errors,
    final String message;

    SemanticErrorType(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
