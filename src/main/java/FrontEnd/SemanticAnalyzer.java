package FrontEnd;

import ErrorHandlers.AbstractErrorHandler;
import ErrorHandlers.ErrorTypes.SemanticErrorType;
import ErrorHandlers.SemanticErrorHandler;
import ErrorHandlers.WarningTypes.SemanticWarningType;

public class SemanticAnalyzer {
    private final SemanticErrorHandler errorHandler;

    public SemanticAnalyzer(AbstractErrorHandler<SemanticErrorType, SemanticWarningType> errorHandler1) {
        errorHandler = (SemanticErrorHandler) errorHandler1;
    }
}
