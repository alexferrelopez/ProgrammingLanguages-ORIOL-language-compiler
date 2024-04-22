package FrontEnd.ErrorHandlers;

import FrontEnd.ErrorHandlers.ErrorTypes.LexicalErrorType;
import FrontEnd.ErrorHandlers.WarningTypes.LexicalWarningType;

public class LexicalErrorHandler extends AbstractErrorHandler<LexicalErrorType, LexicalWarningType> {
    @Override
    public String reportError(LexicalErrorType errorType) {
        return "";
    }

    @Override
    public String reportWarning(LexicalWarningType warningType) {
        return "";
    }
}