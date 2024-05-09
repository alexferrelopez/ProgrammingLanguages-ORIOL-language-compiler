package errorHandlers;

import errorHandlers.errorTypes.SemanticErrorType;
import errorHandlers.warningTypes.SemanticWarningType;

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
        StringBuilder sb = new StringBuilder();
        sb.append("Semantic error no.").append(this.getErrorCount());

        if (optionalLine != null) sb.append(" at line ").append(optionalLine);
        if (optionalColumn != null) sb.append(", column ").append(optionalColumn);

        sb.append(":\n");
        sb.append("\t").append(errorType.getMessage()).append(": ").append(word);

        String message = sb.toString();

        Report<MessageType> report = new Report<>(errorType, optionalLine, optionalColumn, word, sb.toString());
        addReport(report);

        return message;
    }

    /**
     * See parent class: @{@link AbstractErrorHandler}.
     */
    @Override
    public String reportWarning(SemanticWarningType warningType, int lineNum, int colNum, String word) {
        addWarning();
        return "";
    }

    /*
    public static void main(String[] args) {
        SemanticErrorHandler lexicalErrorHandler = new SemanticErrorHandler();
        System.out.println(lexicalErrorHandler.reportError(SemanticErrorType.NON_MATCHING_TYPE, 1, 3, "hello"));
    }
     */
}
