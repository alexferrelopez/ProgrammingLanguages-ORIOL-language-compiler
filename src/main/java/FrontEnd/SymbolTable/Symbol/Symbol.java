package FrontEnd.SymbolTable.Symbol;

import FrontEnd.Dictionary.TokenEnums.DataType;
import FrontEnd.SymbolTable.Scope.ScopeType;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a symbol in the symbol table. A symbol has a name, a type, a value, and a scopeType.
 * @param <Type> the type of the value of the symbol.
 */

public abstract class Symbol<Type> {
    private final String name;          // The name (lexeme) of the symbol.
    private final DataType dataType;    // The type of the symbol.
    private long memoryAddress;
    private final long lineDeclaration;
    private List<Long> lineUsage;
    private Type value;                 // The value of the symbol (Wrapper classes = Integer, Long, String, Character...).
    private final ScopeType scopeType;  // The type of scope of the symbol.
    private long offset;	            // Used in code generation to calculate the offset of the symbol in the stack.
    // Offset for a variable is the distance from the base pointer to the variable.
    // Offset for a function is the label in the assembler code.

    public Symbol(String name, DataType dataType, long lineDeclaration, ScopeType scopeType) {
        this.name = name;
        this.dataType = dataType;
        this.lineDeclaration = lineDeclaration;
        this.scopeType = scopeType;
        this.lineUsage = new ArrayList<>();
    }

    public String getName() {
        return this.name;
    }

    // Getter and setter for the value
    public Type getValue() {
        return this.value;
    }

    public void setValue(Type value) {
        this.value = value;
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

    public boolean hasSameName(String otherSymbolName) {
        return this.name.equals((otherSymbolName));
    }
}
