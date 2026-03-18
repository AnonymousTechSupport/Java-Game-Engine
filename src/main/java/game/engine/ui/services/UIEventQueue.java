package game.engine.ui.services;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * A simple thread-safe queue for UI events. This allows background threads to
 * enqueue UI updates that will be processed on the main thread. The main thread
 * should call flush() at the end of each update cycle to execute pending UI
 * actions. This is necessary to ensure that UI updates happen on the correct
 * thread and to avoid concurrency issues.
 */
public class UIEventQueue {
    private final Queue<Runnable> queue = new ArrayDeque<>();
    private volatile boolean pending = false;

    public void enqueue(Runnable action) {
        queue.add(action);
        pending = true;
    }

    public boolean hasPending() {
        return pending;
    }

    public void flush() {
        while (!queue.isEmpty()) {
            queue.poll().run();
        }
        pending = false;
    }
}
