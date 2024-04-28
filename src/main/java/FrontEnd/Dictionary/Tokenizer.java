package FrontEnd.Dictionary;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tokenizer {

	// Utility class with a static method to return a type of enum given a string.
	public static TokenType convertStringIntoTokenType(TokenType enumType, String tokenText) {
		Pattern pattern = Pattern.compile(enumType.getPattern());
		Matcher matcher = pattern.matcher(tokenText);

		// Check if the regex of the constant matches the input text.
		if (matcher.matches()) {
			return enumType;
		}

		return null;
	}

	// Utility class with a static method to check if a word is inside a list of tokens.
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
}