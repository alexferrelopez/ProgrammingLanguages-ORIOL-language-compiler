import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class LexicalAnalyzer {
    /**
     * Lexical Analyzer / Scanner
     **/
    private BufferedReader bufferedReader;
    private List<Token> tokens;
    private int currentLine;
    private int index;
    private String line;

    public LexicalAnalyzer(String inputFileName) {
        try {
            bufferedReader = new BufferedReader(new FileReader(inputFileName));
            line = bufferedReader.readLine();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Token nextToken() {
        //return token;
        return tokens.get(index++);
    }
}
