package game.engine.ui.core;

import imgui.ImGui;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;

/**
 * A wrapper for initializing and managing the ImGui instance and its backend.
 * This class handles the lifecycle of ImGui (init, new frame, render, cleanup).
 */
public class ImGuiLayer {
    private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();

    public void init(long glfwWindow) {
        ImGui.createContext();
        ImGui.getIO().addConfigFlags(ImGuiConfigFlags.DockingEnable);
        ImGui.getIO().setFontGlobalScale(1.65f);
        imGuiGlfw.init(glfwWindow, true);
        imGuiGl3.init("#version 330");
    }

    public void beginFrame() {
        imGuiGlfw.newFrame();
        ImGui.newFrame();
    }

    public void endFrame() {
        ImGui.render();
        // Ensure GL state expected by ImGui renderer is enabled to avoid artifacts
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_CULL_FACE);
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_SCISSOR_TEST);
        glActiveTexture(GL_TEXTURE0);

        imGuiGl3.renderDrawData(ImGui.getDrawData());
    }

    public void cleanup() {
        imGuiGl3.dispose();
        imGuiGlfw.dispose();
        ImGui.destroyContext();
    }
}
