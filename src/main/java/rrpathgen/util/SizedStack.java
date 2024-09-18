package rrpathgen.util;

import java.util.Stack;

public class SizedStack<Node> extends Stack<Node> {
    private final int maxSize;

    public SizedStack(int size) {
        super();
        this.maxSize = size;
    }

    @Override
    public Node push(Node object) {
        // if the stack is too big, remove elements until it's the right size
        while (this.size() >= maxSize) {
            this.remove(0);
        }
        return super.push(object);
    }
}