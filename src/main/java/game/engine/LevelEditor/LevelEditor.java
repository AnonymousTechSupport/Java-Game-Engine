package game.engine.LevelEditor;

import imgui.ImGui;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;

public class LevelEditor {
    private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();
    
    public LevelEditor(long glfwWindow) {
        ImGui.createContext();
        imGuiGlfw.init(glfwWindow, true);
        imGuiGl3.init("#version 330");
    }

    public void update() {
        imGuiGlfw.newFrame();
        ImGui.newFrame();

        ImGui.begin("Level Editor");
        if (ImGui.button("Save")) {
            System.out.println("Saved");
        }

        ImGui.end();

        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());
    }

    public void cleanup() {
        imGuiGl3.dispose();
        imGuiGlfw.dispose();
        ImGui.destroyContext();
    }
}
