package FrontEnd.Dictionary;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tokenizer {

	// Utility class with a static method to return a type of enum given a string.
	public static <T extends Enum<T> & TokenType> T convertStringIntoEnum(Class<T> enumType, String tokenText) {
		for (T constant : enumType.getEnumConstants()) {
			Pattern pattern = Pattern.compile(constant.getPattern());
			Matcher matcher = pattern.matcher(tokenText);
			if (matcher.matches()) {
				return constant;
			}
		}
		return null;
	}
}