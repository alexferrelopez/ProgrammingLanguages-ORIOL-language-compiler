package errorHandlers;

import errorHandlers.errorTypes.SyntacticErrorType;
import errorHandlers.warningTypes.ParserWarningType;

/**
 * Error handler for the parser/syntactic analyzer, extends error enums to give accurate error and warning messages.
 */
public class SyntacticErrorHandler extends AbstractErrorHandler<SyntacticErrorType, ParserWarningType> {
    /**
     * See parent class: @{@link AbstractErrorHandler}.
     */
    @Override
    public String reportError(SyntacticErrorType errorType, Integer optionalLine, Integer optionalColumn, String word) {
        addError();
        StringBuilder sb = new StringBuilder();
        sb.append("Syntactic error no.").append(this.getErrorCount());

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
    public String reportWarning(ParserWarningType warningType, int lineNum, int colNum, String word) {
        addWarning();
        return "";
    }

    /*
    public static void main(String[] args) {
        SyntacticErrorHandler lexicalErrorHandler = new SyntacticErrorHandler();
        System.out.println(lexicalErrorHandler.reportError(SyntacticErrorType.MISSING_TOKEN_ERROR, 1, 3, "hello"));
    }
    */
}
