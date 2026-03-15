package game.engine;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

/**
 * Time/delta implementation using GLFW's timer. This relies on GLFW being initialized
 */
public class Time {
    private double lastTime;
    private float delta;
    private boolean paused = false;

    public Time() {
        lastTime = glfwGetTime();
        delta = 0f;
    }

    /** Update the timer; should be called once per frame. */
    public void update() {
        double now = glfwGetTime();
        if (paused) {
            delta = 0f;
            lastTime = now;
            return;
        }
        delta = (float)(now - lastTime);
        lastTime = now;
    }

    /** Returns delta time in seconds (float). */
    public float getDelta() { return delta; }

    public void setPaused(boolean p) { this.paused = p; if (!p) lastTime = glfwGetTime(); }
}
