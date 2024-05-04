package errorHandlers;

import errorHandlers.warningTypes.SemanticWarningType;
import errorHandlers.errorTypes.SemanticErrorType;

/**
 * Error handler for semantic errors, extends error enums to give accurate error and warning messages.
 */
public class SemanticErrorHandler extends AbstractErrorHandler<SemanticErrorType, SemanticWarningType> {
    /**
     * See parent class: @{@link AbstractErrorHandler}.
     */
    @Override
    public String reportError(SemanticErrorType errorType, Integer optionalLine, Integer optionalColumn, String word) {
        addError();
        return "";
    }

    /**
     * See parent class: @{@link AbstractErrorHandler}.
     */
    @Override
    public String reportWarning(SemanticWarningType warningType, int lineNum, int colNum, String word) {
        addWarning();
        return "";
    }
}
