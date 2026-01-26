package org.lwjgl.opengl;

import java.nio.ByteBuffer;

public class EventQueue {
    private static final int QUEUE_SIZE = 200;
    private final ByteBuffer queue;
    private final int eventSize;

    public EventQueue(int size) {
        eventSize = size;
        queue = ByteBuffer.allocate(QUEUE_SIZE * size);
    }

    public synchronized void clearEvents() {
        queue.clear();
    }

    public synchronized void copyEvents(ByteBuffer dest) {
        queue.flip();
        int old_limit = queue.limit();
        if (dest.remaining() < queue.remaining()) queue.limit(dest.remaining() + queue.position());
        dest.put(queue);
        queue.limit(old_limit);
        queue.compact();
    }

    public synchronized void putEvent(ByteBuffer event) {
        if (event.remaining() != eventSize) throw new IllegalArgumentException(
                "Internal error: event size "
                + eventSize
                + " does not equal the given event size "
                + event.remaining()
        );

        if (queue.remaining() >= event.remaining()) queue.put(event);
    }
}