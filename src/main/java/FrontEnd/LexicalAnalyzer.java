package FrontEnd;

import FrontEnd.Exceptions.InvalidFileException;

import java.io.*;
import java.util.Scanner;

public class LexicalAnalyzer {
    /**
     * Lexical Analyzer / Scanner
     **/
    private final String codeFilePath;

    public LexicalAnalyzer(String codeFilePath) {
        this.codeFilePath = codeFilePath;
    }

    // Code by https://stackoverflow.com/a/811860
    public void readCodeFile() throws InvalidFileException {
        // Get file instance based on the argument passed to the program.
        File file = new File(this.codeFilePath);

        // Check if file exists.
        if (file.exists()) {

            // Read all the words until EOF (end of file)
            try (Scanner scanner = new Scanner(file)) {
                while (scanner.hasNext()) {
                    String word = scanner.next();
                    System.out.println("Word read: " + word);
                }
            } catch (FileNotFoundException e) {
                // Although we checked if the file exists, this catch is still needed in case of other file access issues
                throw new InvalidFileException("LEXIC: File access issues");
            }

        // Error if file does not exist.
        } else {
            throw new InvalidFileException();
        }
    }
}
