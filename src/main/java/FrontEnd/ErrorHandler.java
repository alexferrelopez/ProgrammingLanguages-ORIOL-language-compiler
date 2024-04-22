package FrontEnd;

public class ErrorHandler {
    private int errorCount;
    private int warningCount;

    public ErrorHandler() {
        this.errorCount = 0;
        this.warningCount = 0;
    }

    public String reportError(String message) {
        this.errorCount++;
        return "Error: " + message;
    }

    public String reportWarning(String message) {
        this.warningCount++;
        return "Warning: " + message;
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
}
