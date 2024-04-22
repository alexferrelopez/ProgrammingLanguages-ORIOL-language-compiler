package FrontEnd.ErrorHandlers;

import FrontEnd.ErrorHandlers.ErrorTypes.SemanticErrorType;
import FrontEnd.ErrorHandlers.WarningTypes.SemanticWarningType;

public class SemanticErrorHandler extends AbstractErrorHandler<SemanticErrorType, SemanticWarningType> {
    @Override
    public String reportError(SemanticErrorType errorType) {
        return "";
    }

    @Override
    public String reportWarning(SemanticWarningType warningType) {
        return "";
    }
}
