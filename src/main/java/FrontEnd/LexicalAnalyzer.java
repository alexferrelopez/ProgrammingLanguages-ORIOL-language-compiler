package FrontEnd;

import FrontEnd.Dictionary.Token;
import FrontEnd.Dictionary.TokenEnums.*;
import FrontEnd.Dictionary.TokenType;
import FrontEnd.Dictionary.Tokenizer;
import FrontEnd.Exceptions.InvalidFileException;

import java.io.*;
import java.util.Arrays;
import java.util.List;
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
                    Token token = getToken(word);
                    if (token != null) {
                        System.out.println(token);
                    }
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

    private Token getToken(String word) {
        // Check through all the different enums (each object in the array represents an enum that implements TokenType).
        List<Class<? extends Enum<?>>> enumClasses = Arrays.asList(
                SpecialSymbol.class,
                MathOperator.class,
                ValueSymbol.class,
                ReservedSymbol.class,
                DataType.class,
                BinaryOperator.class
        );

        // Loop through each enum class to see if the word is found in any enum.
        TokenType tokenType = null;

        for (Class<?> enumClass : enumClasses) {
            @SuppressWarnings("unchecked")  // Safe cast because we know our list only contains enums implementing TokenType
            Class<? extends Enum<? extends TokenType>> safeEnumClass = (Class<? extends Enum<? extends TokenType>>) enumClass;
            tokenType = Tokenizer.convertStringIntoEnum(safeEnumClass, word);
            if (tokenType != null) {
                break;
            }
        }

        if (tokenType != null) {
            return new Token(tokenType, word);
        }

        // No match found in any enum
        return null;
    }
}
