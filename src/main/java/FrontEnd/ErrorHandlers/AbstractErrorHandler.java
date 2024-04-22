package FrontEnd.ErrorHandlers;

import FrontEnd.ErrorHandlers.ErrorTypes.ErrorType;
import FrontEnd.ErrorHandlers.WarningTypes.WarningType;

/**
 * Abstract class for error handling
 * @param <T> Any class that interfaces with ErrorType
 * @param <K> Any class that interfaces with WarningType
 */
abstract class AbstractErrorHandler<T extends ErrorType, K extends WarningType> {
    private int errorCount;
    private int warningCount;

    public AbstractErrorHandler() {
        this.errorCount = 0;
        this.warningCount = 0;
    }

    /**
     * Abstract method to report an error, must be implemented by subclasses, should report a different
     * error message based on the error type.
     * @param errorType The error type to report, the type must be of the same type as the subclass,
     *                  meaning it must be of type T, which is a class that implements ErrorType
     * @return  A string representation of the error
     */
    abstract public String reportError(T errorType);

    /**
     * Abstract method to report a warning, must be implemented by subclasses
     * @param warningType The warning type to report, the type must be of the same type as the subclass,
     *                    meaning it must be of type K, which is a class that implements WarningType
     * @return  A string representation of the warning
     */
    abstract public String reportWarning(K warningType);

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
     * Method to check if there are any errors
     * @return  True if there are errors, false otherwise
     */
    public boolean hasErrors() {
        return this.errorCount > 0;
    }

    /**
     * Method to get the number of errors
     * @return  The number of errors
     */
    public int getErrorCount() {
        return this.errorCount;
    }

    /**
     * Method to get the number of warnings
     * @return  The number of warnings
     */
    public int getWarningCount() {
        return this.warningCount;
    }

    /**
     * Method to reset the error and warning counts
     */
    public void resetErrors() {
        this.errorCount = 0;
        this.warningCount = 0;
    }
}