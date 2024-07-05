package frontEnd.lexic;

import errorHandlers.AbstractErrorHandler;
import errorHandlers.LexicalErrorHandler;
import errorHandlers.errorTypes.LexicalErrorType;
import errorHandlers.warningTypes.LexicalWarningType;
import frontEnd.exceptions.lexic.InvalidFileException;
import frontEnd.exceptions.lexic.InvalidTokenException;
import frontEnd.lexic.dictionary.Token;
import frontEnd.lexic.dictionary.TokenType;
import frontEnd.lexic.dictionary.Tokenizer;
import frontEnd.lexic.dictionary.tokenEnums.ReservedSymbol;
import frontEnd.lexic.dictionary.tokenEnums.SpecialSymbol;

import java.io.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Lexical Analyzer / Scanner
 **/
public class LexicalAnalyzer implements LexicalAnalyzerInterface {
    private final static Token EOF = new Token(ReservedSymbol.EOF);
    private final String codePath;
    private final LexicalErrorHandler errorHandler;
    private BufferedReader codeReader;
    private boolean separatorFound = false;
    private char previousChar;
    private boolean eof = false;
    private int line = 1;
    private int column = 1;

    // Constructor for file path.
    public LexicalAnalyzer(String codeFilePath, AbstractErrorHandler<LexicalErrorType, LexicalWarningType> errorHandler) {
        this.codePath = codeFilePath;
        this.errorHandler = (LexicalErrorHandler) errorHandler;
    }

    @Override
    public void startLexicalAnalysis() throws InvalidFileException {
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
        if (eof) {
            return EOF;
        }
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
        return getTokenRead(String.valueOf(word), line, column - word.length() - 1);
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
                column++;
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
        try {
            Token token = Tokenizer.convertStringIntoToken(word);
            token.setPosition(line, column);
            return token;

        } catch (InvalidTokenException e) {
            // If no token was found, send the error to the lexicErrorHandler.
            errorHandler.reportError(LexicalErrorType.UNKNOWN_TOKEN_ERROR, line, column, word);
            throw new InvalidTokenException(e.getMessage());
        }
    }
}
