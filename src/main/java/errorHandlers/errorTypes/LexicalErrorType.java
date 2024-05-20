package errorHandlers.errorTypes;

/**
 * Enum for lexical errors
 */
public enum LexicalErrorType implements ErrorType {
    UNKNOWN_TOKEN_ERROR("Unknown token"),
    RESERVED_TOKEN_ERROR("Reserved token"),
    ;

    final String message;

    LexicalErrorType(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
