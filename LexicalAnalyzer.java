import java.io.BufferedReader;
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
    private int lineIndex;

    public LexicalAnalyzer(String inputFileName) {
        try {
            bufferedReader = new BufferedReader(new FileReader(inputFileName));
            line = bufferedReader.readLine();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void nextLine() {
        try {
            line = bufferedReader.readLine();
            lineIndex = 0;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNextWord() {
        if (line == null) {
            return null;
        }
        String[] words = line.split(" ");
        if (words.length == 0) {
            nextLine();
            return getNextWord();
        }
        String word = words[lineIndex++];
        if (lineIndex == words.length) {
            nextLine();
        }
        return word;
    }

    public Token nextToken() {
        //return token;
        return tokens.get(index++);
    }
}
