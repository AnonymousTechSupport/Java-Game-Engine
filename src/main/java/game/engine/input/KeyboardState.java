package game.engine.input;

import java.util.Arrays;

/** TODO: Look into potentially switching to a bit set
 * to track key states more compactly, especially if we expand beyond A-Z and ESC. For now, this is simple and clear enough.
 */

/**
 * Internal class to track the state of keys. This is not exposed outside of the
 * input package. It maintains arrays for whether each key is currently down,
 * was pressed this frame, or was released this frame. The InputManager class
 * manages the lifecycle of these states and provides methods to query them.
 */
class KeyboardState {
    private final boolean[] down;
    private final boolean[] pressed;
    private final boolean[] released;

    KeyboardState(int count) {
        down = new boolean[count];
        pressed = new boolean[count];
        released = new boolean[count];
    }

    void setDownIndex(int idx, boolean isDown) {
        boolean prev = down[idx];
        if (prev != isDown) {
            down[idx] = isDown;
            if (isDown)
                pressed[idx] = true;
            else
                released[idx] = true;
        }
    }

    boolean isDownIndex(int idx) {
        return down[idx];
    }

    boolean wasPressedIndex(int idx) {
        return pressed[idx];
    }

    boolean wasReleasedIndex(int idx) {
        return released[idx];
    }

    void resetFrame() {
        Arrays.fill(pressed, false);
        Arrays.fill(released, false);
    }

    void clearAll() {
        Arrays.fill(down, false);
        resetFrame();
    }
}
