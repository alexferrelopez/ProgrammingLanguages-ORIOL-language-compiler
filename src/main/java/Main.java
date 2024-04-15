import FrontEnd.LexicalAnalyzer;
import FrontEnd.RecursiveDescentLLParser;

public class Main {

    private final static int EXPECTED_NUM_ARGS = 1;
    private final static String TOO_MANY_ARGS_ERROR = "There are too many arguments! Select only ono code to compile.";
    private final static String NO_ARGS_ERROR = "No input file handled! There is no code selected to compile.";

    public static void main(String[] args) {
        if (args.length == EXPECTED_NUM_ARGS) {
            String codeFilePath = args[EXPECTED_NUM_ARGS - 1];
            startCompiler(codeFilePath);
        }
        else if (args.length > EXPECTED_NUM_ARGS){
            System.out.println(TOO_MANY_ARGS_ERROR);
        }
        // Case where numArguments == 0
        else {
            System.out.println(NO_ARGS_ERROR);
        }
    }

    private static void startCompiler(String codeFilePath) {
        // ---- FRONT END ---- //
        LexicalAnalyzer lexicalAnalyzer = new LexicalAnalyzer(codeFilePath);
        RecursiveDescentLLParser recursiveDescentLLParser = new RecursiveDescentLLParser(lexicalAnalyzer);
    }
}
