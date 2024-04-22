package FrontEnd.ErrorHandlers;

import FrontEnd.ErrorHandlers.ErrorTypes.ErrorType;
import FrontEnd.ErrorHandlers.WarningTypes.WarningType;

abstract class ErrorHandler<T extends ErrorType, K extends WarningType> {
    private int errorCount;
    private int warningCount;

    public ErrorHandler() {
        this.errorCount = 0;
        this.warningCount = 0;
    }

    abstract public String reportError(T errorType);

    abstract public String reportWarning(K warningType);

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
}