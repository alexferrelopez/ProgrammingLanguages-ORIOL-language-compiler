public interface CompilerInterface {

	/**
	 * This method starts the lexical, syntactic and semantic analysis of the code. Generates the intermediate code.
	 */
	void compileCode();


	/**
	 * This method returns if the code has errors or not (checks the lexical, syntactic and semantic errors).
	 * @return True if the code has errors, false otherwise.
	 */
	boolean hasErrors();
}
