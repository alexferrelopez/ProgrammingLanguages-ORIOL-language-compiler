package FrontEnd;

public interface ParserErrorHandler {
    enum ParserErrorType {
        //TODO: Add different types of errors
    }
    enum ParserWarningType {
        //TODO: Add different types of warnings
    }
    String reportParsingError(ParserErrorType message);
    String reportParsingWarning(ParserWarningType message);
}
