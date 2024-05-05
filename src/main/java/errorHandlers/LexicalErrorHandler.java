package errorHandlers;

import errorHandlers.errorTypes.LexicalErrorType;
import errorHandlers.warningTypes.LexicalWarningType;

/**
 * Error handler for lexical errors, extends error enums to give accurate error and warning messages.
 */
public class LexicalErrorHandler extends AbstractErrorHandler<LexicalErrorType, LexicalWarningType> {
    /**
     * See parent class: @{@link AbstractErrorHandler}.
     */
    @Override
    public String reportError(LexicalErrorType errorType, Integer optionalLine, Integer optionalColumn, String word) {
        addError();
        StringBuilder sb = new StringBuilder();
        sb.append("Lexical error no.").append(this.getErrorCount());

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
    public String reportWarning(LexicalWarningType warningType, int lineNum, int colNum, String word) {
        addWarning();
        return "";
    }
    /*
    public static void main(String[] args) {
        LexicalErrorHandler lexicalErrorHandler = new LexicalErrorHandler();
        System.out.println(lexicalErrorHandler.reportError(LexicalErrorType.UNKNOWN_TOKEN_ERROR, 1, null, "hello"));
        System.out.println(lexicalErrorHandler.reportError(LexicalErrorType.RESERVED_TOKEN_ERROR, 1, 1, "hello"));
    }
    */
}