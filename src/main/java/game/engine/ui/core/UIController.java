package game.engine.ui.core;

import game.engine.logging.Logger;

/**
 * UIController centralizes cross-cutting UI policies and orchestrates how the
 * UI reacts to global engine state changes. It should be registered with the
 * StateManager as a StateListener by the LevelEditor.
 */
public class UIController implements game.engine.StateManager.StateListener {
    private final UIManager uiManager;

    public UIController(UIManager uiManager) {
        this.uiManager = uiManager;
    }

    @Override
    public void onStateChanged(game.engine.EngineState from, game.engine.EngineState to) {
        // Forward to UI panels so they can update themselves if necessary
        try {
            uiManager.onStateChanged(from, to);
        } catch (Exception e) {
            Logger.error(Logger.UI, "Error notifying panels of state change: " + e.getMessage());
        }

        // Cross-cutting policy: when entering PLAYING, cancel any active inline edits
        if (to == game.engine.EngineState.PLAYING) {
            uiManager.cancelEdits();
        }
    }
}
