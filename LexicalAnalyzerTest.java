import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LexicalAnalyzerTest {

    @Test
    void wordByWordRead() {
        ArrayList<String> testWords = getWords();
        ArrayList<String> expectedWords = new ArrayList<>(List.of("ASD QE SX SDF S SG".split(" ")));
        for (int i = 0; i < testWords.size(); i++) {
            String word = testWords.get(i);
            String expectedWord = expectedWords.get(i);
            assertEquals(word, expectedWord);
        }
    }

    private ArrayList<String> getWords() {
        ArrayList<String> words = new ArrayList<>();
        LexicalAnalyzer lexicalAnalyzer = new LexicalAnalyzer("ExempleParaules.farm");
        String nextWord;
        do {
            nextWord = lexicalAnalyzer.getNextWord();
            if (nextWord != null) {
                words.add(nextWord);
            }
        } while (nextWord != null);
        return words;
    }
}