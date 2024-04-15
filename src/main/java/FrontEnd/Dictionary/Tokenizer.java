package FrontEnd.Dictionary;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tokenizer {

	// Utility class with a static method to return a type of enum given a string.
	public static TokenType convertStringIntoEnum(Class<? extends Enum<? extends TokenType>> enumType, String tokenText) {
		for (Enum<? extends TokenType> enumConstant : enumType.getEnumConstants()) {
			TokenType tokenType = (TokenType) enumConstant;
			Pattern pattern = Pattern.compile(tokenType.getPattern());
			Matcher matcher = pattern.matcher(tokenText);

			// Check if the regex of the constant matches the input text.
			if (matcher.matches()) {
				return tokenType;
			}
		}
		return null;
	}
}