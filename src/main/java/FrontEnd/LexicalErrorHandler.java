package FrontEnd;

public interface LexicalErrorHandler {
    enum LexicalErrorType {
        UNKNOWN_TOKEN_ERROR
        //TODO: Add different types of errors,
    }
    enum LexicalWarningType {
        //TODO: Add different types of warnings
        VARIABLE_NAME_TOO_LONG_WARNING,
        VARIABLE_NAME_TOO_SHORT_WARNING,
    }
    String reportLexicalError(LexicalErrorType message);
    String reportLexicalWarning(LexicalWarningType message);
}
