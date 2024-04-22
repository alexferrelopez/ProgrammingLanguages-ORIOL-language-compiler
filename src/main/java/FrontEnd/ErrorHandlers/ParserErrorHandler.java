package FrontEnd.ErrorHandlers;

import FrontEnd.ErrorHandlers.ErrorTypes.ParserErrorType;
import FrontEnd.ErrorHandlers.WarningTypes.ParserWarningType;

public class ParserErrorHandler extends AbstractErrorHandler<ParserErrorType, ParserWarningType> {
    @Override
    public String reportError(ParserErrorType errorType) {
        return "";
    }

    @Override
    public String reportWarning(ParserWarningType warningType) {
        return "";
    }
}
