package net.minecraft.util;

public class Tuple<A, B> {
    private final A a;
    private final B b;

    public Tuple(A aIn, B bIn) {
        a = aIn;
        b = bIn;
    }

    public A getFirst() {
        return a;
    }

    public B getSecond() {
        return b;
    }
}
