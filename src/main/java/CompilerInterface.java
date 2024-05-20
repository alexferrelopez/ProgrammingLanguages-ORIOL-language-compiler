public interface CompilerInterface {

    /**
     * This method starts the lexical, syntactic and semantic analysis of the code. Generates the intermediate code.
     */
    void compileCode();


    /**
     * This method returns if the code has errors or not (checks the lexical, syntactic and semantic errors).
     *
     * @return True if the code has errors, false otherwise.
     */
    boolean hasErrors();

    /**
     * This method returns if the code has warnings or not (checks the lexical, syntactic and semantic warnings).
     *
     * @return True if the code has warnings, false otherwise.
     */
    boolean hasWarnings();

    /**
     * This method prints the different errors that the compiler has found.
     */
    void printErrors();

    /**
     * This method prints the different warnings that the compiler has found.
     */
    void printWarnings();
}
