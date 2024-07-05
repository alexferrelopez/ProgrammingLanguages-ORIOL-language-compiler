package errorHandlers;

import errorHandlers.errorTypes.ErrorType;
import errorHandlers.warningTypes.WarningType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class for error handling
 *
 * @param <T> Any class that interfaces with ErrorType
 * @param <K> Any class that interfaces with WarningType
 */
public abstract class AbstractErrorHandler<T extends ErrorType, K extends WarningType> {
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private final List<Report<ErrorType>> errorReports;
    private final List<Report<WarningType>> warningReports;
    private int errorCount;
    private int warningCount;

    public AbstractErrorHandler() {
        this.errorReports = new ArrayList<>();
        this.warningReports = new ArrayList<>();
        this.errorCount = 0;
        this.warningCount = 0;
    }

    /**
     * Abstract method to report an error, must be implemented by subclasses, should report a different
     * error message based on the error type.
     *
     * @param errorType      The error type to report, the type must be of the same type as the subclass,
     *                       meaning it must be of type T, which is a class that implements ErrorType
     * @param optionalLine   The line number of the error, can be null, should be handled by the subclass
     * @param optionalColumn The column number of the error, can be null, should be handled by the subclass
     * @param word           The word that caused the error
     * @return A string representation of the error
     */
    abstract public String reportError(T errorType, @Nullable Integer optionalLine, @Nullable Integer optionalColumn, String word);

    /**
     * Abstract method to report a warning, must be implemented by subclasses
     *
     * @param warningType    The warning type to report, the type must be of the same type as the subclass,
     *                       meaning it must be of type K, which is a class that implements WarningType
     * @param optionalLine   The line number of the warning, can be null, should be handled by the subclass
     * @param optionalColumn The column number of the warning, can be null, should be handled by the subclass
     * @param word           The word that caused the warning
     * @return A string representation of the warning
     */
    abstract public String reportWarning(K warningType, @Nullable Integer optionalLine, @Nullable Integer optionalColumn, String word);

    /**
     * Method to add an error to the error count, protected so that only subclasses can access it
     */
    protected void addError() {
        this.errorCount++;
    }

    /**
     * Method to add a warning to the warning count, protected so that only subclasses can access it
     */
    protected void addWarning() {
        this.warningCount++;
    }

    /**
     * Method to add an error report to the error reports list
     *
     * @param report The error report to add
     */
    protected void addErrorReport(Report<ErrorType> report) {
        this.errorReports.add(report);
    }

    /**
     * Method to add a warning report to the warning reports list
     *
     * @param report The warning report to add
     */
    protected void addWarningReport(Report<WarningType> report) {
        this.warningReports.add(report);
    }


    /**
     * Method to check if there are any errors
     *
     * @return True if there are errors, false otherwise
     */
    public boolean hasErrors() {
        return this.errorCount > 0;
    }

    /**
     * Method to check if there are any warnings
     *
     * @return True if there are warnings, false otherwise
     */
    public boolean hasWarnings() {
        return this.warningCount > 0;
    }

    /**
     * Method to get the number of errors
     *
     * @return The number of errors
     */
    public int getErrorCount() {
        return this.errorCount;
    }

    /**
     * Method to get the number of warnings
     *
     * @return The number of warnings
     */
    public int getWarningCount() {
        return this.warningCount;
    }

    /**
     * Method to get the reports
     *
     * @return A clone of the reports list
     */
    public List<Report<ErrorType>> getErrorReports() {
        return new ArrayList<>(this.errorReports);
    }

    /**
     * Method to reset the error and warning counts
     */
    public void resetErrors() {
        this.errorCount = 0;
        this.warningCount = 0;
    }

    /**
     * Method to print all the reports in the reports list
     */
    public void printErrors() {
        for (Report<ErrorType> report : errorReports) {
            System.out.println(ANSI_RED + report + ANSI_RESET);
        }
    }

    /**
     * Method to print all the warnings in the warnings list
     */
    public void printWarnings() {
        for (Report<WarningType> report : warningReports) {
            System.out.println(ANSI_YELLOW + report + ANSI_RESET);
        }
    }
}