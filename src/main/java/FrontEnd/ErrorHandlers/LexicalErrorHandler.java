package FrontEnd.ErrorHandlers;

import FrontEnd.ErrorHandlers.ErrorTypes.LexicalErrorType;
import FrontEnd.ErrorHandlers.WarningTypes.LexicalWarningType;

/**
 * Error handler for lexical errors, extends error enums to give accurate error and warning messages.
 */
public class LexicalErrorHandler extends AbstractErrorHandler<LexicalErrorType, LexicalWarningType> {
    /**
     * See parent class.
     */
    @Override
    public String reportError(LexicalErrorType errorType) {
        addError();
        return "";
    }

    /**
     * See parent class.
     */
    @Override
    public String reportWarning(LexicalWarningType warningType) {
        addWarning();
        return "";
    }
}