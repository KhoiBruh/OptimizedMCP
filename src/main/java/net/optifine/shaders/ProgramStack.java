package net.optifine.shaders;

import java.util.ArrayDeque;
import java.util.Deque;

public class ProgramStack {
    private final Deque<Program> stack = new ArrayDeque<>();

    public void push(Program p) {
        stack.addLast(p);

        if (stack.size() > 100) {
            throw new RuntimeException("Program stack overflow: " + stack.size());
        }
    }

    public Program pop() {
        if (stack.isEmpty()) {
            throw new RuntimeException("Program stack empty");
        } else {
            return stack.pollLast();
        }
    }
}
