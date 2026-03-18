package game.engine;

import game.engine.logging.Logger;
import java.util.ArrayList;
import java.util.List;

/**
 * Central state manager controlling PLAYING / EDITOR / PAUSED. Create one
 * instance and share it with subsystems that need to know the current state.
 */
public class StateManager {
    private EngineState state = EngineState.EDITOR;
    private final List<StateListener> listeners = new ArrayList<>();
    private final WindowHandler window;
    private final Time time;

    public StateManager(WindowHandler window, Time time) {
        this.window = window;
        this.time = time;
        onEnter(this.state);
        Logger.info(Logger.ENGINE, "StateManager initialized in " + this.state);
    }

    public synchronized EngineState getState() {
        return state;
    }

    public synchronized boolean isPlaying() {
        return state == EngineState.PLAYING;
    }

    public synchronized boolean isEditor() {
        return state == EngineState.EDITOR;
    }

    public synchronized boolean isPaused() {
        return state == EngineState.PAUSED;
    }

    /** Returns the last measured frame delta from the Time object. */
    public float getDelta() {
        return time.getDelta();
    }

    /** Set state and run enter/exit logic once. */
    public synchronized void setState(EngineState next) {
        if (next == null || this.state == next)
            return;
        EngineState prev = this.state;
        this.state = next;
        Logger.info(Logger.ENGINE, "State: " + prev + " -> " + next);
        onEnter(next);
        for (StateListener l : listeners)
            l.onStateChanged(prev, next);
    }

    private void onEnter(EngineState s) {
        switch (s) {
        case PLAYING:
            window.setCursorHidden(true);
            time.setPaused(false);
            break;
        case EDITOR:
            window.setCursorHidden(false);
            time.setPaused(true);
            break;
        case PAUSED:
            window.setCursorHidden(false);
            time.setPaused(true);
            break;
        }
    }

    public void addListener(StateListener l) {
        listeners.add(l);
    }

    public void removeListener(StateListener l) {
        listeners.remove(l);
    }

    /** Listener interface for state changes. */
    public interface StateListener {
        void onStateChanged(EngineState from, EngineState to);
    }
}
