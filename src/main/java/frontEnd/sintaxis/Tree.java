package frontEnd.sintaxis;

import java.util.LinkedList;
import java.util.List;

public class Tree<T> {
    private final T node;
    private final List<Tree<T>> children;
    private Tree<T> parent;

    public Tree(T node){
        this.node = node;
        children = new LinkedList<>();
        parent = null;
    }

    public Tree(Tree<T> t){
        this.node = t.node;
        children = new LinkedList<>();
        parent = t.parent;
        for (var c : t.children){
            children.add(new Tree<>(c));
        }
    }

    public Tree<T> addChild(T child){
        var c = new Tree<T>(child);
        children.add(c);
        c.parent = this;
        return c;
    }

    public void removeParent() {
        parent = null;
    }

    public T getNode() {
        return node;
    }

    public List<Tree<T>> getChildren() {
        return children;
    }

    public Tree<T> getParent() {
        return parent;
    }
}
