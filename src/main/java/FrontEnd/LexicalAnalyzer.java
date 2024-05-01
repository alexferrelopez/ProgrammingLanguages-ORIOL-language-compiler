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
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LexicalAnalyzer {
    /**
     * Lexical Analyzer / Scanner
     **/
    private final String codePath;
    private BufferedReader codeReader;
    private boolean separatorFound = false;
    private char previousChar;
    private boolean eof = false;
    private final static Token EOF = new Token(ReservedSymbol.EOF);
    private int line = 1;
    private int column = 1;

    // Constructor for file path.
    public LexicalAnalyzer(String codeFilePath) {
        this.codePath = codeFilePath;
    }

    public void openCodeFile() throws InvalidFileException {
        // Open the file and check if it exists.
        checkFileExists();
    }

    private void checkFileExists() throws InvalidFileException {
        // Get file instance based on the argument passed to the program.
        File codeFile = new File(this.codePath);

        // Check if file exists.
        if (!codeFile.exists()) {
            throw new InvalidFileException("File does not exist.");
        }

        try {
            codeReader = new BufferedReader(new FileReader(codeFile));

        } catch (FileNotFoundException e) {
            throw new InvalidFileException("LEXIC: File access issues.");
        }
    }

    /**
     * Get the next token from the file.
     *
     * @return the next token.
     * @throws InvalidTokenException if the token is not valid.
     */
    public Token getNextToken() throws InvalidTokenException {
        // Check if the previous character was a separator from the previous token. Usually happens with cases like:
        // "miau a;" -> "miau" is a token, "a" is a token, ";" is the separator and the token.
        if (separatorFound) {
            separatorFound = false;
            // The column is decremented by 1 because the column is incremented after reading the last character that
            // is a separator.
            return getTokenRead(String.valueOf(previousChar), line, column - 1);
        }

        String word;

        // Read the next word from the file.
        word = readUntilSeparator();
        // This is a special case where the last token ended on a whitespace and the next token is a separator.
        // This happens in cases such as "miau a ;", where the last token is "a" and the next token is ";" with no
        // letters in the word.
        if (separatorFound && word.isEmpty()) {
            separatorFound = false;
            // The column is decremented by 1 because the column is incremented after reading the last character that
            // is a separator.
            return getTokenRead(String.valueOf(previousChar), line, column - 1);
        }

        // We might have reached the end of the file but if there is a word, we still need to return it.
        // It's only when the word is empty that we return EOF, meaning there is actually nothing else to read.
        if (eof && word.isEmpty()) {
            return EOF;
        }

        // The column is decremented by 1 because the column is incremented after reading the last character that
        // is a separator, it is also decremented by the length of the word to point to the first character of the word.
        return getTokenRead(String.valueOf(word), line, column - 1 - word.length());
    }

    /**
     * Reads the file character by character until a separator is found.
     *
     * @return the word read from the file.
     */
    private String readUntilSeparator() {
        StringBuilder stringBuilder = new StringBuilder();
        // Has to be an integer to check for EOF.
        int character;

        do {
            try {
                character = codeReader.read();
                char c = (char) character;
                //Check if the end of the file has been reached.
                if (character == -1) {
                    eof = true;
                    return stringBuilder.toString();
                    // Check if the character is a hidden character (line break, tab, space). Since they are separators
                } else if (isHiddenCharacter(c)) {
                    // Here there are two cases:
                    // 1. The string builder is empty, so we continue reading until we find a non-hidden character to
                    // return a real token.
                    // 2. The string builder is not empty, so we return the current word, since we consider the
                    // character as a separator.
                    if (!stringBuilder.isEmpty()) {
                        return stringBuilder.toString().trim();
                    }
                    // Check if the character is a separator from the separators list. These are also tokens so they must be
                    // returned as a token. There are two cases too:
                    // 1. The string builder is empty, so we get the separator later in the upper function
                    // 2. The string builder is not empty, so we return the current word and store the separator for the
                    // next call to the function.
                } else if (isSeparator(character)) {
                    separatorFound = true;
                    previousChar = c;
                    return stringBuilder.toString().trim();
                    // If the character is not a separator, we add it to the word.
                } else {
                    column++;
                    stringBuilder.append(c);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } while (true);
    }

    /**
     * Check if the character is a hidden character (line break, tab, space). In Windows, the line break is "\r\n",
     * unlike other Unix systems that use \n. We check for both cases by looking if our character is contained in
     * our string of hidden separators, similar to a regex.
     *
     * @param character the character to check.
     * @return true if the character is a hidden character, false otherwise.
     */
    private boolean isHiddenCharacter(int character) {
        if ("\n".contains(String.valueOf((char) character))) {
            line++;
            column = 1;
        }
        String lineBreaks = "\n\r\t ";
        return lineBreaks.contains(String.valueOf((char) character));
    }

    /**
     * Check if the character is a separator from the separators list. We check if the character is contained in our
     * string of separators.
     *
     * @param character the character to check.
     * @return true if the character is a separator, false otherwise.
     */
    private boolean isSeparator(int character) {
        List<TokenType> separators = Stream.of(SpecialSymbol.values())
                .collect(Collectors.toList());
        return Tokenizer.isWordInsideTokenList(separators, String.valueOf((char) character));
    }

    /**
     * Checks against all the regexes of the different enums to see if the word is a valid token.
     *
     * @param word   the word to check against the regexes.
     * @param line   the line where the word was found.
     * @param column the column where the word was found.
     * @return the token if the word is valid.
     * @throws InvalidTokenException if the word is not a valid token.
     */
    private Token getTokenRead(String word, int line, int column) throws InvalidTokenException {
        // Check through all the different enums (each object in the array represents an enum that implements TokenType).
        List<TokenType> enumValues = Stream.of(
                        // The order of the list is important, since the first match will be the selected one.
                        // "moo" has to be determined as "DATA_TYPE", not "VARIABLE".
                        ReservedSymbol.values(),
                        DataType.values(),
                        SpecialSymbol.values(),
                        MathOperator.values(),
                        BinaryOperator.values(),
                        ValueSymbol.values()
                )
                .flatMap(Arrays::stream)
                .collect(Collectors.toList());

        // Loop through each enum class to see if the word is found in any enum.
        TokenType tokenType;

        // Our enums list only contains enums implementing TokenType
        for (TokenType enumConstant : enumValues) {
            tokenType = Tokenizer.convertStringIntoTokenType(enumConstant, word);

            // Check if the current token is valid (different to null)
            if (tokenType != null) {
                System.out.println("Token found: " + tokenType + " " + word);
                return new Token(tokenType, word, line, column);
            }
        }

        // No match found in any enum's regex = throw exception.
        throw new InvalidTokenException("Invalid token found: " + word);
    }
}
