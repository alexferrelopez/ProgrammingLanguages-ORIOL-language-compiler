package errorHandlers.errorTypes;

/**
 * Enum for parser/syntactic analyzer errors
 */
public enum SyntacticErrorType implements ErrorType {
    UNEXPECTED_TOKEN_ERROR("Unexpected token"),
    MISSING_TOKEN_ERROR("Missing token"),
    UNMATCHED_OPENING_BRACKET_ERROR(""),
    UNMATCHED_CLOSING_BRACKET_ERROR(""),
    INVALID_OPERATOR_USAGE_ERROR(""),
    CORE_DUMPED("Core dumped ;)"),
    NO_AXIOMA_ERROR("No axiom found"),;

    final String message;

    SyntacticErrorType(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
