package game.engine.input;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallbackI;

/**
 * TODO: Look into potential for implementing an action system, so we can bind keys to an action
 * example: MOVE_FORWARD -> W key
 */

/**
 * Manages keyboard input for the game.
 * This class sets up a GLFW key callback to track the state of keys and provides methods to
 * query whether specific keys are currently pressed, were just pressed, or were just released.
 * It also includes a method to reset per-frame input states, which should be called at the start of each frame.
 * The ESC key is handled to trigger a window close request when pressed or released.
 */
public class InputManager {
    private final long windowHandle;
    private final GLFWKeyCallbackI callback;
    private final KeyboardState keyboard = new KeyboardState(Key.COUNT);
    private final game.engine.StateManager stateManager;

    public InputManager(long windowHandle, game.engine.StateManager stateManager) {
        this.windowHandle = windowHandle;
        this.stateManager = stateManager;
        this.callback = (win, key, scancode, action, mods) -> onKeyEvent(win, key, scancode, action, mods);

        GLFW.glfwSetKeyCallback(windowHandle, callback);
    }

    /** Call at the start of each frame to reset per-frame pressed/released flags. */
    public void beginFrame() {
        keyboard.resetFrame();
    }

    /** Free the callback and any resources. */
    public void free() {
        GLFW.glfwSetKeyCallback(windowHandle, null);
    }

    // Expose keyboard query methods (limited surface)
    public boolean isDown(Key key) { return keyboard.isDownIndex(key.getIndex()); }
    public boolean wasPressed(Key key) { return keyboard.wasPressedIndex(key.getIndex()); }
    public boolean wasReleased(Key key) { return keyboard.wasReleasedIndex(key.getIndex()); }
    public void clearAll() { keyboard.clearAll(); }

    public void onKeyEvent(long win, int keyCode, int scancode, int action, int mods) {
        // Highest priority: state transitions
        if (action == GLFW.GLFW_PRESS) {
            if (keyCode == GLFW.GLFW_KEY_F1) {
                // toggle editor
                if (stateManager.isEditor()) stateManager.setState(game.engine.EngineState.PLAYING);
                else stateManager.setState(game.engine.EngineState.EDITOR);
                return;
            }
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                // If we're in the editor, ESC should first exit the editor and return to PLAYING
                // instead of toggling PAUSED. From PLAYING, ESC will still toggle PAUSED as expected.
                if (stateManager.isEditor()) {
                    stateManager.setState(game.engine.EngineState.PLAYING);
                } else if (stateManager.isPaused()) {
                    stateManager.setState(game.engine.EngineState.PLAYING);
                } else {
                    stateManager.setState(game.engine.EngineState.PAUSED);
                }
                return;
            }
        }

        // If UI wants keyboard (e.g., in editor), consume
        if ((stateManager.isEditor() || stateManager.isPaused()) && imgui.ImGui.getIO().getWantCaptureKeyboard()) {
            return;
        }

        Key k = Key.fromKeyCode(keyCode);
        if (k != null) {
            keyboard.setDownIndex(k.getIndex(), action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT);
        }
    }
}
