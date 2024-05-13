package errorHandlers.errorTypes;

/**
 * Enum for parser/syntactic analyzer errors
 */
public enum SyntacticErrorType implements ErrorType {
    //TODO: Add different types of parsing errors
    UNEXPECTED_TOKEN_ERROR(""),
    MISSING_TOKEN_ERROR(""),
    UNMATCHED_OPENING_BRACKET_ERROR(""),
    UNMATCHED_CLOSING_BRACKET_ERROR(""),
    INVALID_OPERATOR_USAGE_ERROR("");

    final String message;

    SyntacticErrorType(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
