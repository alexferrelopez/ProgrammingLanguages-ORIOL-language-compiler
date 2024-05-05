package frontEnd.lexic.dictionary;

import frontEnd.exceptions.InvalidTokenException;
import frontEnd.lexic.dictionary.tokenEnums.*;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Tokenizer {

    /**
     * Utility class with a static method to return a type of enum given a string.
     *
     * @param enumType  enum type to check against.
     * @param tokenText word to check if it is inside the enum.
     * @return the enum type if the word is inside the enum.
     */
    public static TokenType convertStringIntoTokenType(TokenType enumType, String tokenText) {
        Pattern pattern = Pattern.compile(enumType.getPattern());
        Matcher matcher = pattern.matcher(tokenText);

        // Check if the regex of the constant matches the input text.
        if (matcher.matches()) {
            return enumType;
        }

        return null;
    }

    /**
     * Utility class with a static method to check if a word is inside a list of tokens.
     *
     * @param tokens    list of tokens to check against.
     * @param tokenText word to check if it is inside the list of tokens.
     * @return true if the word is inside the list of tokens.
     */
    public static boolean isWordInsideTokenList(List<TokenType> tokens, String tokenText) {
        for (TokenType tokenType : tokens) {
            Pattern pattern = Pattern.compile(tokenType.getPattern());
            Matcher matcher = pattern.matcher(tokenText);

            // Check if the regex of the constant matches the input text.
            if (matcher.matches()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks against all the regexes of the different enums to see if the word is a valid token.
     *
     * @param tokenWord the word to check against the regexes.
     * @return the token if the word is valid.
     * @throws InvalidTokenException if the word is not a valid token.
     */
    public static Token convertStringIntoToken(String tokenWord) throws InvalidTokenException {
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
            tokenType = Tokenizer.convertStringIntoTokenType(enumConstant, tokenWord);

            // Check if the current token is valid (different to null)
            if (tokenType != null) {
                return new Token(tokenType, tokenWord);
            }
        }

        throw new InvalidTokenException("Invalid token found: " + tokenWord);
    }
}