package FrontEnd;

public class ErrorHandler implements LexicalErrorHandler, ParserErrorHandler, SemanticErrorHandler {
    private int errorCount;
    private int warningCount;

    public ErrorHandler() {
        this.errorCount = 0;
        this.warningCount = 0;
    }

    public boolean hasErrors() {
        return this.errorCount > 0;
    }

    public int getErrorCount() {
        return this.errorCount;
    }

    public int getWarningCount() {
        return this.warningCount;
    }

    public void resetErrors() {
        this.errorCount = 0;
        this.warningCount = 0;
    }

    @Override
    public String reportLexicalError(LexicalErrorType message) {
        return "";
    }

    @Override
    public String reportLexicalWarning(LexicalWarningType message) {
        return "";
    }

    @Override
    public String reportParsingError(ParserErrorType message) {
        return "";
    }

    @Override
    public String reportParsingWarning(ParserWarningType message) {
        return "";
    }

    @Override
    public String reportSemanticError(SemanticErrorType message) {
        return "";
    }

    @Override
    public String reportSemanticWarning(SemanticWarningType message) {
        return "";
    }
}
