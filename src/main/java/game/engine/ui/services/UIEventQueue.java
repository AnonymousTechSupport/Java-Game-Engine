package game.engine.ui.services;

import java.util.ArrayDeque;
import java.util.Queue;

public class UIEventQueue {
    private final Queue<Runnable> queue = new ArrayDeque<>();
    private volatile boolean pending = false;

    public void enqueue(Runnable action) {
        queue.add(action);
        pending = true;
    }

    public boolean hasPending() { return pending; }

    public void flush() {
        while (!queue.isEmpty()) {
            queue.poll().run();
        }
        pending = false;
    }
}
