package tree;

public abstract class Node {
    protected Node next;

    public Node() {
        next = new EndNode();
    }

    protected Node(boolean skipInit) {
        next = null;
    }

    public Node next() {
        return next;
    }

    public void next(Node node) {
        next = node;
    }
}
