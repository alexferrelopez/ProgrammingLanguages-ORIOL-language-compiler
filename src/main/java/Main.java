public class Main {

    // Public constants to check in the tests.
    public final static String TOO_MANY_ARGS_ERROR = "There are too many arguments! Select only one code to compile.";
    public final static String NO_ARGS_ERROR = "No input file handled! There is no code selected to compile.";
    public final static String INVALID_EXTENSION_ERROR = "Invalid format of the code to compile.";

    private final static int EXPECTED_NUM_ARGS = 1;         // The main argument is not the name of the Java's file (Java is not like C).
    private final static String FILE_EXTENSION = ".farm";

    public static void main(String[] args) {
        checkValidArguments(args);
    }

    private static void checkValidArguments(String[] args) {
        if (args.length == EXPECTED_NUM_ARGS) {
            String codeFilePath = args[EXPECTED_NUM_ARGS - 1];

            // Check if the extension of the file is .farm.
            if (codeFilePath.endsWith(FILE_EXTENSION)) {
                startCompiler(codeFilePath);
            } else {
                System.out.println(INVALID_EXTENSION_ERROR);
            }
        } else if (args.length > EXPECTED_NUM_ARGS) {
            System.out.println(TOO_MANY_ARGS_ERROR);
        }
        // Case where numArguments == 0
        else {
            System.out.println(NO_ARGS_ERROR);
        }
    }

    private static void startCompiler(String codeFilePath) {
        CompilerInterface compiler = new Compiler(codeFilePath);
        compiler.compileCode();

        if (compiler.hasWarnings()) {
            compiler.printWarnings();
        }

        // Prints for Debugging purpose
        if (compiler.hasErrors()) {
            System.out.println("Compilation failed!");
            compiler.printErrors();
        } else {
            System.out.println("Compilation successful!");
        }
    }
}
