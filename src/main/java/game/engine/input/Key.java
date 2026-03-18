package game.engine.input;

import org.lwjgl.glfw.GLFW;

/**
 * Key enum for A-Z and ESC. Each enum holds the GLFW key code and mutable state
 * for current-down and per-frame transitions. The index field provides a stable
 * index for Keyboard arrays, while the glfwCode is used for GLFW callbacks.
 */
public enum Key {
    A(GLFW.GLFW_KEY_A, 0), B(GLFW.GLFW_KEY_B, 1), C(GLFW.GLFW_KEY_C, 2), D(GLFW.GLFW_KEY_D, 3), E(GLFW.GLFW_KEY_E, 4), F(GLFW.GLFW_KEY_F, 5),
    G(GLFW.GLFW_KEY_G, 6), H(GLFW.GLFW_KEY_H, 7), I(GLFW.GLFW_KEY_I, 8), J(GLFW.GLFW_KEY_J, 9), K(GLFW.GLFW_KEY_K, 10), L(GLFW.GLFW_KEY_L, 11),
    M(GLFW.GLFW_KEY_M, 12), N(GLFW.GLFW_KEY_N, 13), O(GLFW.GLFW_KEY_O, 14), P(GLFW.GLFW_KEY_P, 15), Q(GLFW.GLFW_KEY_Q, 16), R(GLFW.GLFW_KEY_R, 17),
    S(GLFW.GLFW_KEY_S, 18), T(GLFW.GLFW_KEY_T, 19), U(GLFW.GLFW_KEY_U, 20), V(GLFW.GLFW_KEY_V, 21), W(GLFW.GLFW_KEY_W, 22), X(GLFW.GLFW_KEY_X, 23),
    Y(GLFW.GLFW_KEY_Y, 24), Z(GLFW.GLFW_KEY_Z, 25), ESC(GLFW.GLFW_KEY_ESCAPE, 26), F1(GLFW.GLFW_KEY_F1, 27);

    private final int glfwCode;
    private final int index; // stable index used by Keyboard arrays

    Key(int glfwCode, int index) {
        this.glfwCode = glfwCode;
        this.index = index;
    }

    public int getKeyCode() {
        return glfwCode;
    }

    public int getIndex() {
        return index;
    }

    /** Total number of keys tracked by the Key enum. */
    public static final int COUNT = values().length;

    // fast lookup table for code -> Key
    private static final Key[] LOOKUP = new Key[GLFW.GLFW_KEY_LAST + 1];

    static {
        for (Key k : values()) {
            int code = k.glfwCode;
            if (code >= 0 && code < LOOKUP.length)
                LOOKUP[code] = k;
        }
    }

    public static Key fromKeyCode(int code) {
        if (code < 0 || code >= LOOKUP.length)
            return null;
        return LOOKUP[code];
    }
}
