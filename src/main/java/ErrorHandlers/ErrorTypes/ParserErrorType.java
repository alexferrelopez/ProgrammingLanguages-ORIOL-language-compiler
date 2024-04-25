package ErrorHandlers.ErrorTypes;

/**
 * Enum for parser errors
 */
public enum ParserErrorType implements ErrorType {
    //TODO: Add different types of parsing errors
    UNEXPECTED_TOKEN_ERROR,
    MISSING_TOKEN_ERROR,
    UNMATCHED_OPENING_BRACKET_ERROR,
    UNMATCHED_CLOSING_BRACKET_ERROR,
    INVALID_OPERATOR_USAGE_ERROR
}
