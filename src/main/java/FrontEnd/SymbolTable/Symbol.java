package FrontEnd.SymbolTable;

import FrontEnd.Dictionary.TokenEnums.DataType;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a symbol in the symbol table. A symbol has a name, a type, a value, and a scope.
 * @param <T> the type of the value of the symbol.
 */

public abstract class Symbol<T> {
    private final String name;          // The name (lexeme) of the symbol.
    private final DataType dataType;    // The type of the symbol.
    private long memoryAddress;
    private final long lineDeclaration;
    private List<Long> lineUsage;
    private T value;                    // The value of the symbol (Wrapper classes = Integer, Long, String, Character...).
    private final Scope scope;          // The scope of the symbol.
    private long offset;	            // Used in code generation to calculate the offset of the symbol in the stack.

    // TODO -> recordar que un Symbol és un node d'un arbre, per tant, ha de tenir una llista de fills.

    public Symbol(String name, DataType dataType, long lineDeclaration, Scope scope) {
        this.name = name;
        this.dataType = dataType;
        this.lineDeclaration = lineDeclaration;
        this.scope = scope;
        this.lineUsage = new ArrayList<>();
    }

    public String getName() {
        return this.name;
    }

    // Getter and setter for the value
    public T getValue() {
        return this.value;
    }

    public void setValue(T value) {
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
}
