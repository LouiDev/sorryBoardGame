package linkedlist;

public class EndNode extends Node {
    public EndNode() {
        super(true);
    }

    @Override
    public Node next() {
        return this;
    }
}
