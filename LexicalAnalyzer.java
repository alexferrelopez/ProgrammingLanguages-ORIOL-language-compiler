import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


public class LexicalAnalyzer {
    /**
     * Lexical Analyzer / Scanner
     **/
    private BufferedReader bufferedReader;
    //private List<Token> tokens;
    //private int currentLine;
    //private int index;
    private String line;
    private int lineIndex;
    private List<String> words;

    public LexicalAnalyzer(String inputFileName) {
        try {
            bufferedReader = new BufferedReader(new FileReader(inputFileName));
            nextLine();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void nextLine() {
        try {
            line = bufferedReader.readLine();
            lineIndex = 0;
            words = Collections.emptyList();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNextWord() {

        // EoF reached
        if (line == null) {
            return null;
        }

        if (words.isEmpty()) {
            words = Arrays.stream(line.split("(?=[ ;)({}:,])")).toList();
        }

        if (words.isEmpty()) {
            nextLine();
            return getNextWord();
        }

        String word = words.get(lineIndex++);
        if (lineIndex >= words.size()) {
            nextLine();
        }
        //return word.trim();
        return word;
    }
    /*
    public Token nextToken() {
        //return token;
        return null;
    }*/
}
