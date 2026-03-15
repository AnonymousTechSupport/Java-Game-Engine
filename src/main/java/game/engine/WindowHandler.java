package game.engine;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryStack;
import java.nio.IntBuffer;
import org.lwjgl.opengl.GL;

import game.engine.logging.Logger;

/** Handles the creation and management of the game window
 * This class is responsible for initializing GLFW, creating a borderless fullscreen window, and managing the rendering loop
 * It also provides methods for polling events and swapping buffers
 * The window is designed to cover the entire primary monitor without any decorations (title bar, borders)
*/
public class WindowHandler {
  private long handle;
  private int width, height;
  private String title;
  private boolean isRunning = false;

  public WindowHandler(String title) { this.title = title; }

    public void createWindow() {
    Logger.info(Logger.ENGINE, "Initializing GLFW...");
    if (!glfwInit()) {
      Logger.fatal(Logger.ENGINE, "Unable to initialize GLFW");
    }

    glfwDefaultWindowHints();
    glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
    glfwWindowHint(GLFW_DECORATED, GLFW_FALSE); // No title bar/borders
    glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);

    long monitor = glfwGetPrimaryMonitor();
    Logger.check(monitor != NULL, "Failed to get primary monitor");

    GLFWVidMode vidMode = glfwGetVideoMode(monitor);
    Logger.check(vidMode != null, "Failed to get video mode");
    
    width = vidMode.width();
    height = vidMode.height();
    
    Logger.debug(Logger.ENGINE, () -> "Creating " + width + "x" + height + " window titled '" + title + "'");
    handle = glfwCreateWindow(width, height, title, monitor, NULL);
    if (handle == NULL) {
      glfwTerminate();
      Logger.fatal(Logger.ENGINE, "Failed to create borderless fullscreen window");
    }

    // Position to cover the monitor exactly (important for multi-monitor)
    try (MemoryStack stack = MemoryStack.stackPush()) {
      IntBuffer mx = stack.mallocInt(1);
      IntBuffer my = stack.mallocInt(1);
      glfwGetMonitorPos(monitor, mx, my);
      glfwSetWindowPos(handle, mx.get(0), my.get(0));
    }

    glfwMakeContextCurrent(handle);
    glfwSwapInterval(1); // Enable VSync

    GL.createCapabilities();
    Logger.info(Logger.RENDER, "OpenGL initialized: " + glGetString(GL_VERSION));

    glfwShowWindow(handle);
    isRunning = true;
  }


  /** Render the window's content. */
  public void render() {
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
  }

  public boolean isRunning() { return isRunning; }
  public boolean shouldClose() { return glfwWindowShouldClose(handle); }

  /**
   * Poll for window events.
   * This should be called regularly (e.g., once per frame) to ensure the window remains responsive.
   * This will process events such as input, window close requests, and other interactions.
  */
  public void pollEvents() { glfwPollEvents(); }

  /**
   * Swap the window's buffers.
   * This should be called at the end of each frame to display the rendered content.
   */
  public void swapBuffers() { glfwSwapBuffers(handle); }

  public void cleanup() {
    Logger.info(Logger.ENGINE, "Cleaning up window and terminating GLFW.");
    if (handle != NULL) {
      glfwDestroyWindow(handle);
    }
    glfwTerminate();
    isRunning = false;
  }

  /** Show or hide the OS cursor. */
  public void setCursorHidden(boolean hidden) {
    glfwSetInputMode(handle, GLFW_CURSOR, hidden ? GLFW_CURSOR_DISABLED : GLFW_CURSOR_NORMAL);
  }

  // Getters for window properties
  public long getHandle() { return handle; }
  public int getWidth() { return width; }
  public int getHeight() { return height; }
}
