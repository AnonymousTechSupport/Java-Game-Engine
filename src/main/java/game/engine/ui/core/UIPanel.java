package game.engine.ui.core;

/**
 * Lifecycle interface for UI panels. Panels implementing this interface declare
 * that they participate in global UI lifecycle events such as state changes.
 *
 * Implementations should keep lifecycle methods lightweight because they are
 * invoked on the main render thread.
 */
public interface UIPanel {
    /** Render the panel for the current frame. */
    void render();

    /**
     * Called when the global engine state changes (e.g. from EDITOR ->
     * PLAYING). Panels may react to the state transition (cancel edits,
     * hide/show content).
     */
    default void onStateChanged(game.engine.EngineState from, game.engine.EngineState to) {
    }
}
