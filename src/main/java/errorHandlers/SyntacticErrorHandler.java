package errorHandlers;

import errorHandlers.errorTypes.ErrorType;
import errorHandlers.errorTypes.SyntacticErrorType;
import errorHandlers.warningTypes.SyntacticWarningType;
import errorHandlers.warningTypes.WarningType;
import org.jetbrains.annotations.Nullable;

/**
 * Error handler for the parser/syntactic analyzer, extends error enums to give accurate error and warning messages.
 */
public class SyntacticErrorHandler extends AbstractErrorHandler<SyntacticErrorType, SyntacticWarningType> {
    /**
     * See parent class: @{@link AbstractErrorHandler}.
     */
    @Override
    public String reportError(SyntacticErrorType errorType, @Nullable Integer optionalLine, @Nullable Integer optionalColumn, String word) {
        addError();
        StringBuilder sb = new StringBuilder();
        sb.append("Syntactic error no.").append(this.getErrorCount());

        if (optionalLine != null) sb.append(" at line ").append(optionalLine);
        if (optionalColumn != null) sb.append(", column ").append(optionalColumn);

        sb.append(":\n");
        sb.append("\t").append(errorType.getMessage()).append(": ").append(word);

        String message = sb.toString();

        Report<ErrorType> report = new Report<>(errorType, optionalLine, optionalColumn, word, sb.toString());
        addErrorReport(report);

        return message;
    }

    /**
     * See parent class: @{@link AbstractErrorHandler}.
     */
    @Override
    public String reportWarning(SyntacticWarningType warningType, @Nullable Integer optionalLine, @Nullable Integer optionalColumn, String word) {
        addWarning();
        StringBuilder sb = new StringBuilder();
        sb.append("Lexical warning no.").append(this.getWarningCount());

        if (optionalLine != null) sb.append(" at line ").append(optionalLine);
        if (optionalColumn != null) sb.append(", column ").append(optionalColumn);

        sb.append(":\n");
        sb.append("\t").append(warningType.getMessage()).append(": ").append(word);

        String message = sb.toString();

        Report<WarningType> report = new Report<>(warningType, optionalLine, optionalColumn, word, sb.toString());
        addWarningReport(report);

        return message;
    }

    /*
    public static void main(String[] args) {
        SyntacticErrorHandler lexicalErrorHandler = new SyntacticErrorHandler();
        System.out.println(lexicalErrorHandler.reportError(SyntacticErrorType.MISSING_TOKEN_ERROR, 1, 3, "hello"));
        System.out.println(lexicalErrorHandler.reportWarning(SyntacticWarningType.test, 1, 3, "hello"));
    }
    */
}
