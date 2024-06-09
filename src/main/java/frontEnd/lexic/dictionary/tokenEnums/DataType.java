package frontEnd.lexic.dictionary.tokenEnums;

import frontEnd.lexic.dictionary.TokenType;

import java.util.List;

public enum DataType implements TokenType {
    // Enum constants (data types)
    INTEGER("miau", List.of("miau"), 4, Integer.MAX_VALUE, Integer.MIN_VALUE),                // Each integer is 4 Bytes.
    FLOAT("oink", List.of("oink"), 4, Float.MAX_VALUE, -Float.MAX_VALUE),                    // Each float is 4 Bytes.
    BOOLEAN("status", List.of("status"), 1, 1, 0),                        // 1 = true, 0 = false. Each boolean is 1 Byte.
    CHAR("moo", List.of("moo"), 1, (int) Character.MAX_VALUE, (int) Character.MIN_VALUE),  // Each character is 1 Byte.
    STRING("quack", List.of("quack"), 1, Integer.MAX_VALUE, 0),                    // Each character is 1 Byte (multiply it by the String's length).
    VOID("void", List.of("void"), 0, 0, 0);                            // Each void is 0 Bytes.

    private final static String TRUE_BOOLEAN = "alive"; // String representation of a boolean true
    // Instance field (regex pattern) for each enum constant
    private final String regexPattern;
    private final List<String> translation;
    private final int size; // Size of the data type in Bytes
    private final Number maxValue; // Maximum value that this data type can hold
    private final Number minValue; // Minimum value that this data type can hold

    // Constructor to initialize the instance fields (allow it to have a string value and its size in Bytes).
    DataType(String pattern, List<String> translation, int size, Number maxValue, Number minValue) {
        this.regexPattern = pattern;
        this.translation = translation;
        this.size = size;
        this.maxValue = maxValue;
        this.minValue = minValue;
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

    // Getter method for the pattern
    @Override
    public String getPattern() {
        return this.regexPattern;
    }

    @Override
    public List<String> getTranslation() {
        return this.translation;
    }

    // Getter method for the size of the data type
    public int getSize() {
        return this.size;
    }

    // Getter method for the maximum value of the data type
    public boolean isBetweenRange(Number size) {
        return greaterThan(maxValue, size) && lessThan(minValue, size);
    }

    public boolean isValidType(ValueSymbol valueSymbol) {
        return switch (this) {
            case INTEGER -> valueSymbol == ValueSymbol.VALUE_INT;
            case FLOAT -> valueSymbol == ValueSymbol.VALUE_FLOAT;
            case STRING -> valueSymbol == ValueSymbol.VALUE_STRING;
            case CHAR -> valueSymbol == ValueSymbol.VALUE_CHAR;
            case BOOLEAN -> valueSymbol == ValueSymbol.VALUE_TRUE || valueSymbol == ValueSymbol.VALUE_FALSE;
            default -> false;
        };
    }

    public Object convertValue(String value) {
        // Conversion logic based on dataType
        return switch (this) {
            case INTEGER -> Integer.parseInt(value);
            case FLOAT -> Float.parseFloat(value);
            case BOOLEAN -> value.equalsIgnoreCase(TRUE_BOOLEAN);
            case CHAR -> value.charAt(0);
            case STRING -> value;
            default -> value; // No conversion needed
        };
    }
}
