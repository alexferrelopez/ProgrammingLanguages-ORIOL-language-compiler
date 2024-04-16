package FrontEnd;

import FrontEnd.Dictionary.Token;
import FrontEnd.Dictionary.TokenEnums.*;
import FrontEnd.Dictionary.TokenType;
import FrontEnd.Dictionary.Tokenizer;
import FrontEnd.Exceptions.InvalidFileException;
import FrontEnd.Exceptions.InvalidTokenException;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class LexicalAnalyzer {
    /**
     * Lexical Analyzer / Scanner
     **/
    private final String codeFilePath;
    private File codeFile;
    private Scanner codeFileScanner;
    private final static Token EOF = new Token(ReservedSymbol.EOF);

    public LexicalAnalyzer(String codeFilePath) {
        this.codeFilePath = codeFilePath;
    }

    // Code by https://stackoverflow.com/a/811860
    public void openCodeFile() throws InvalidFileException {
        // Open the file and check if it exists.
        checkFileExists();
    }

    private void checkFileExists() throws InvalidFileException {
        // Get file instance based on the argument passed to the program.
        codeFile = new File(this.codeFilePath);

        // Check if file exists.
        if (!codeFile.exists()) {
            throw new InvalidFileException();
        }

        try {
            codeFileScanner = new Scanner(codeFile);
        } catch (FileNotFoundException e) {
            throw new InvalidFileException("LEXIC: File access issues");
        }
    }

    public Token getNextToken() throws InvalidTokenException {
        Token token = null;

        // Read the next word until EOF (end of file).
        if (codeFileScanner.hasNext()) {
            String word = codeFileScanner.next();
            System.out.print("Word read: " + word + " | ");

            token = getToken(word);
            System.out.println(token);
        }
        else {
            // End of the file reached.
            token = EOF;
        }

        return token;
    }

    private Token getToken(String word) throws InvalidTokenException {
        // Check through all the different enums (each object in the array represents an enum that implements TokenType).
        List<Class<? extends Enum<? extends TokenType>>> enumClasses = Arrays.asList(
                // The order of the list is important, since the first match will be the selected one.
                // "moo" has to be determined as "DATA_TYPE", not "VARIABLE".
                ReservedSymbol.class,
                DataType.class,
                SpecialSymbol.class,
                MathOperator.class,
                ValueSymbol.class,
                BinaryOperator.class
        );

        // Loop through each enum class to see if the word is found in any enum.
        TokenType tokenType = null;

        // Our enums list only contains enums implementing TokenType
        for (Class<? extends Enum<? extends TokenType>> enumClass : enumClasses) {
            tokenType = Tokenizer.convertStringIntoEnum(enumClass, word);
            if (tokenType != null) {
                break;
            }
        }

        if (tokenType != null) {
            return new Token(tokenType, word);
        }

        // No match found in any enum
        throw new InvalidTokenException();
    }
}
