package FrontEnd;

import java.util.LinkedList;

public class Tree<T> {
    private final T node;
    private final LinkedList<Tree<T>> children;
    private T parent;

    public Tree(T node){
        this.node = node;
        children = new LinkedList<>();
        parent = null;
    }

    public Tree<T> addChild(T child){
        var c = new Tree<T>(child);
        children.add(c);
        c.parent = (T) this;
        return c;
    }

    public T getNode() {
        return node;
    }

    public LinkedList<Tree<T>> getChildren() {
        return children;
    }


}
