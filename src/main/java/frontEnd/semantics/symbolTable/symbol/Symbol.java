package frontEnd.semantics.symbolTable.symbol;

import frontEnd.exceptions.semantics.InvalidValueException;
import frontEnd.exceptions.semantics.InvalidValueTypeException;
import frontEnd.lexic.dictionary.Token;
import frontEnd.lexic.dictionary.tokenEnums.DataType;
import frontEnd.lexic.dictionary.tokenEnums.ValueSymbol;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a symbol in the symbol table. A symbol has a name, a type, a value, and a scopeType.
 * @param <Type> the type of the value of the symbol.
 */

public abstract class Symbol<Type> {
    private final String name;          // The name (lexeme) of the symbol.
    private final DataType dataType;    // The type of the symbol (if it's a function, it's the return type).
    private long memoryAddress;
    private final long lineDeclaration;
    private final List<Long> lineUsage;
    private Type value;                 // The value of the symbol (Wrapper classes = Integer, Long, String, Character...).
    private long offset;                // Used in code generation to calculate the offset of the symbol in the stack (its value is usually negative).
    // Offset for a variable is the distance from the base pointer to the variable.
    // Offset for a function is the label in the assembler code.
    private final Class<Type> typeClass; // Class token to maintain type safety

    public Symbol(String name, DataType dataType, long lineDeclaration, Class<Type> typeClass) {
        this.name = name;
        this.dataType = dataType;
        this.lineDeclaration = lineDeclaration;
        this.typeClass = typeClass;
        this.lineUsage = new ArrayList<>();
    }

    public String getName() {
        return this.name;
    }

    // Getter and setter for the value
    public Type getValue() {
        return this.value;
    }

    public void checkValue(Token newValue) throws InvalidValueException, InvalidValueTypeException {
        // Check if the value is compatible with the variable type.
        if (!isValidType((ValueSymbol) newValue.getType())) {
            throw new InvalidValueTypeException("The value is not compatible with the variable type.");
        }

        // Check if the value is between a range (only in numbers).
        String value = newValue.getLexeme();
        if (dataType == DataType.FLOAT || dataType == DataType.INTEGER) {
            Number numberValue = (Number) dataType.convertValue(value);
            if (!dataType.isBetweenRange(numberValue)) {
                throw new InvalidValueException("The value is not in the range of the data type.");
            }
        }

        //this.value = typeClass.cast(dataType.convertValue(value));
    }

    public void addLineUsage(long line) {
        this.lineUsage.add(line);
    }

    public int getSizeInBytes() {
        if (value instanceof String) {
            return ((String) value).getBytes().length * dataType.getSize(); // Multiply the size of the data type (a character is 1 Byte) by the length of the string.
        } else {
            return dataType.getSize();
        }
    }

    public long getLineDeclaration() {
        return lineDeclaration;
    }

    public boolean isDatatype(String dataType) {
        return this.dataType.getPattern().equalsIgnoreCase(dataType);
    }

    public DataType getDataType() {
        return dataType;
    }

    /**
     * Check if a symbol is a variable.
     *
     * @return true if the symbol is a variable; false otherwise.
     */
    public abstract boolean isVariable();

    public boolean hasSameName(String otherSymbolName) {
        return this.name.equals((otherSymbolName));
    }

    public boolean isValidType(ValueSymbol valueSymbol) {
        return this.dataType.isValidType(valueSymbol);
    }

    public long getOffset() {
        return offset;
    }
}
