package FrontEnd;

public interface SemanticErrorHandler {
    enum SemanticErrorType {
        //TODO: Add different types of errors
    }
    enum SemanticWarningType {
        //TODO: Add different types of warnings
    }
    String reportSemanticError(SemanticErrorType message);
    String reportSemanticWarning(SemanticWarningType message);
}
