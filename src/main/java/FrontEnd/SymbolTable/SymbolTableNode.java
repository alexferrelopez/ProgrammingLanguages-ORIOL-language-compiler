package FrontEnd.SymbolTable;

import java.util.ArrayList;
import java.util.List;

public class SymbolTableNode implements SymbolTableInterface {
    private final List<Symbol<?>> symbols;
    public SymbolTableNode left;
    public SymbolTableNode right;
    public SymbolTableNode parent;
    public int height;

    /**
     * Constructor of the SymbolTableNode class.
     */
    public SymbolTableNode(Symbol<?> symbol) {
        this.symbols = new ArrayList<>();
        this.symbols.add(symbol);
        this.left = null;
        this.right = null;
        this.parent = null;
        this.height = 0;    // Initialize height value for null nodes.
    }

    public SymbolTableNode(Symbol<?> symbol, SymbolTableNode parentSymbolTableNode) {
        this(symbol);  // Call the first constructor
        this.parent = parentSymbolTableNode;
    }

    /**
     * Get the symbol of the current node.
     *
     * @return the symbol of the current node.
     */

    public boolean isLeaf() {
        return (right == null && left == null);
    }

    public int getHeight() {
        return height;
    }

    // Get the height of the current node: maximum between right and left node + 1 (the node itself).
    public void calculateHeight() {

        // Case were there is no children (leaf node)
        if (right == null && left == null) {
            height = 0;
            return;
        }

        // Case where there is no right child
        if (right == null) {
            height = left.height + 1;
            return;
        }

        // Case where is no left child
        if (left == null) {
            height = right.height + 1;
            return;
        }

        // Case where the node has both children
        height = Math.max(right.height, left.height) + 1;
    }

    /**
     * Check if the symbol table contains a symbol with the given name and scope.
     *
     * @param name     the name of the symbol.
     * @param numScope the scope of the symbol.
     * @return true if the symbol table contains the symbol, false otherwise.
     */
    @Override
    public boolean contains(String name, int numScope) {
        return false;
    }

    /**
     * Get the symbol with the given name and scope.
     *
     * @param name the name of the symbol.
     * @return the symbol with the given name and scope, or null if the symbol table does not contain the symbol.
     */
    @Override
    public Symbol<?> get(String name) {
        return null;
    }

    /**
     * Get all symbols with the given name.
     *
     * @return a list of symbols with the given name.
     */
    @Override
    public List<Symbol<?>> getAll() {
        return this.symbols;
    }

    /**
     * Insert a symbol into the symbol table.
     *
     * @param symbol the symbol to insert.
     */
    @Override
    public void insert(Symbol<?> symbol) {
        this.symbols.add(symbol);
    }

    /**
     * Get the number of symbols in the symbol table.
     *
     * @return the number of symbols in the symbol table.
     */
    @Override
    public int size() {
        return symbols.size();
    }

    /**
     * Check if the symbol table is empty.
     *
     * @return true if the symbol table is empty, false otherwise.
     */
    @Override
    public boolean isEmpty() {
        return false;
    }
}
