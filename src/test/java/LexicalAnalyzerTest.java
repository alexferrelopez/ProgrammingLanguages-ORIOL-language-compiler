//import FrontEnd.LexicalAnalyzer;
//import org.junit.jupiter.api.Test;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class LexicalAnalyzerTest {
//
//    // Checks if words are separated by tokens
//    @Test
//    void wordByWordRead() {
//        ArrayList<String> testWords = getWords();
//        String s = """
//                oink: sumRange(oink numA, oink numB, oink increment) {
//                    oink result is 0.0;
//                               \s
//                    check (numA bigger numB) {
//                        oink tmp is numA;
//                        numA is numB;
//                        numB is tmp;
//                    }
//                               \s
//                    feed (oink i is numA to numB, i is i sum increment) {
//                        result is result sum i;
//                    }
//                               \s
//                    poop result;
//                }
//                               \s
//                miau: ranch() {
//                    oink a is 3.23;
//                    oink b is 4.5;
//                    oink c is 0.2;
//                    oink total is sumRange(a, b, c);
//                    poop 0;
//                }
//               \s""";
//        ArrayList<String> expectedWords = new ArrayList<>(List.of(s));
//        for (int i = 0; i < testWords.size(); i++) {
//            String word = testWords.get(i);
//            String expectedWord = expectedWords.get(i);
//            assertEquals(word, expectedWord);
//        }
//    }
//
//    // This checks all content is split into tokens and no content is lost from the file, \n are removed since we read line by line
//    @Test
//    void allContentIsTokenized() {
//        // join all words into a single string
//        ArrayList<String> testWords = getWords();
//        StringBuilder sb = new StringBuilder();
//        for (String word : testWords) {
//            sb.append(word);
//        }
//        String testContent = sb.toString();
//        String s = """
//                oink: sumRange(oink numA, oink numB, oink increment) {
//                    oink result is 0.0;
//                               \s
//                    check (numA bigger numB) {
//                        oink tmp is numA;
//                        numA is numB;
//                        numB is tmp;
//                    }
//                               \s
//                    feed (oink i is numA to numB, i is i sum increment) {
//                        result is result sum i;
//                    }
//                               \s
//                    poop result;
//                }
//                               \s
//                miau: ranch() {
//                    oink a is 3.23;
//                    oink b is 4.5;
//                    oink c is 0.2;
//                    oink total is sumRange(a, b, c);
//                    poop 0;
//                }
//               \s""".replace("\n", "");
//        assertEquals(testContent, s);
//
//    }
//
//    private ArrayList<String> getWords() {
//        ArrayList<String> words = new ArrayList<>();
//        LexicalAnalyzer lexicalAnalyzer = new LexicalAnalyzer("Exemple1.farm");
////        String nextWord;
////        do {
////            nextWord = lexicalAnalyzer.getNextWord();
////            if (nextWord != null) {
////                words.add(nextWord);
////            }
////        } while (nextWord != null);
//        return words;
//    }
//}