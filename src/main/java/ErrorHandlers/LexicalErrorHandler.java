package ErrorHandlers;

import ErrorHandlers.ErrorTypes.LexicalErrorType;
import ErrorHandlers.WarningTypes.LexicalWarningType;

/**
 * Error handler for lexical errors, extends error enums to give accurate error and warning messages.
 */
public class LexicalErrorHandler extends AbstractErrorHandler<LexicalErrorType, LexicalWarningType> {
    /**
     * See parent class: @{@link AbstractErrorHandler}.
     */
    @Override
    public String reportError(LexicalErrorType errorType) {
        addError();
        switch (errorType) {
            case UNKNOWN_TOKEN_ERROR:
                return "Unknown token error";
            default:
                return "Unknown error";
        }
    }

    /**
     * See parent class: @{@link AbstractErrorHandler}.
     */
    @Override
    public String reportWarning(LexicalWarningType warningType) {
        addWarning();
        return "";
    }
}