package FrontEnd.ErrorHandlers;

import FrontEnd.ErrorHandlers.ErrorTypes.ParserErrorType;
import FrontEnd.ErrorHandlers.WarningTypes.ParserWarningType;

/**
 * Error handler for the parser, extends error enums to give accurate error and warning messages.
 */
public class ParserErrorHandler extends AbstractErrorHandler<ParserErrorType, ParserWarningType> {
    /**
     * See parent class.
     */
    @Override
    public String reportError(ParserErrorType errorType) {
        addError();
        return "";
    }

    /**
     * See parent class.
     */
    @Override
    public String reportWarning(ParserWarningType warningType) {
        addWarning();
        return "";
    }
}
