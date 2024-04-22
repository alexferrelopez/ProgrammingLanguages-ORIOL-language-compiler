package FrontEnd.ErrorHandlers;

import FrontEnd.ErrorHandlers.ErrorTypes.SemanticErrorType;
import FrontEnd.ErrorHandlers.WarningTypes.SemanticWarningType;

/**
 * Error handler for semantic errors, extends error enums to give accurate error and warning messages.
 */
public class SemanticErrorHandler extends AbstractErrorHandler<SemanticErrorType, SemanticWarningType> {
    /**
     * See parent class.
     */
    @Override
    public String reportError(SemanticErrorType errorType) {
        return "";
    }

    /**
     * See parent class.
     */
    @Override
    public String reportWarning(SemanticWarningType warningType) {
        return "";
    }
}
