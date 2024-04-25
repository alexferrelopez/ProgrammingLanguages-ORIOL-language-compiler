package FrontEnd.Dictionary.TokenEnums;

import FrontEnd.Dictionary.TokenType;

public enum DataType implements TokenType {
	// Enum constants (data types)
	INTEGER("miau", 4),	 	// Each integer is 4 Bytes.
	FLOAT("oink", 4),		// Each float is 4 Bytes.
	BOOLEAN("status", 1),	// Each integer is 4 Bytes.
	CHAR("moo", 1),			// Each character is 1 Byte.
	STRING("quack", 1),		// Each character is 1 Byte (multiply it by the String's length).
	VOID("void", 0);		// Each void is 0 Bytes.

	// Instance field (regex pattern) for each enum constant
	private final String regexPattern;
	private final int size; // Size of the data type in Bytes

	// Constructor to initialize the instance fields (allow it to have a string value and its size in Bytes).
	DataType(String pattern, int size) {
		this.regexPattern = pattern;
		this.size = size;
	}

	// Getter method for the pattern
	@Override
	public String getPattern() {
		return this.regexPattern;
	}

	// Getter method for the size of the data type
	public int getSize() {
		return this.size;
	}
}
