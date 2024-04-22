package FrontEnd;

import FrontEnd.ErrorHandlers.AbstractErrorHandler;
import FrontEnd.ErrorHandlers.ErrorTypes.SemanticErrorType;
import FrontEnd.ErrorHandlers.SemanticErrorHandler;
import FrontEnd.ErrorHandlers.WarningTypes.SemanticWarningType;

public class SemanticAnalyzer {
    private final SemanticErrorHandler errorHandler;

    public SemanticAnalyzer(AbstractErrorHandler<SemanticErrorType, SemanticWarningType> errorHandler1) {
        errorHandler = (SemanticErrorHandler) errorHandler1;
    }
}
