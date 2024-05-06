package frontEnd.lexic.dictionary.tokenEnums;

import frontEnd.lexic.dictionary.TokenType;

import java.util.List;

public enum DataType implements TokenType {
    // Enum constants (data types)
    INTEGER("miau", 4, Integer.MAX_VALUE, Integer.MIN_VALUE),	// Each integer is 4 Bytes.
    FLOAT("oink", 4, Float.MAX_VALUE, -Float.MAX_VALUE),		// Each float is 4 Bytes.
    BOOLEAN("status", 1, 1, 0),				// 1 = true, 0 = false. Each boolean is 1 Byte.
    CHAR("moo", 1, (int) Character.MAX_VALUE, (int) Character.MIN_VALUE),	// Each character is 1 Byte.
    STRING("quack", 1, Integer.MAX_VALUE, 0),			// Each character is 1 Byte (multiply it by the String's length).
    VOID("void", 0, 0, 0);		// Each void is 0 Bytes.

    // Instance field (regex pattern) for each enum constant
    private final String regexPattern;
    private final int size; // Size of the data type in Bytes
    private final Number maxValue; // Maximum value that this data type can hold
    private final Number minValue; // Minimum value that this data type can hold

    // Constructor to initialize the instance fields (allow it to have a string value and its size in Bytes).
    DataType(String pattern, int size, Number maxValue, Number minValue) {
        this.regexPattern = pattern;
        this.size = size;
        this.maxValue = maxValue;
        this.minValue = minValue;
    }

    // Getter method for the pattern
    @Override
    public String getPattern() {
        return this.regexPattern;
    }

    @Override
    public List<String> getTranslation() {
        return List.of();
    }

    // Getter method for the size of the data type
    public int getSize() {
        return this.size;
    }

    // Getter method for the maximum value of the data type
    public boolean isBetweenRange(Number size) {
        return greaterThan(maxValue, size) && lessThan(minValue, size);
    }

    // Utility method to check if one number is greater than another
    private static boolean greaterThan(Number num1, Number num2) {
        return convertToDouble(num1) > convertToDouble(num2);
    }

    // Utility method to check if one number is less than another
    private static boolean lessThan(Number num1, Number num2) {
        return convertToDouble(num1) < convertToDouble(num2);
    }

    // Convert any Number to double
    private static double convertToDouble(Number number) {
        return number.doubleValue();
    }
}
