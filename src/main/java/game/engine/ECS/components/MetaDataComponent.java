package game.engine.ECS.components;

import imgui.ImGui;
import imgui.type.ImString;

public class MetaDataComponent implements Component {
    public String name;

    public MetaDataComponent() {
        this.name = "Entity";
    }

    public MetaDataComponent(String name) {
        this.name = name;
    }

    @Override
    public void renderInspector() {
        // Persistent buffer so clicking into the name field is immediate
        if (nameBuffer == null)
            nameBuffer = new imgui.type.ImString(name, 256);
        if (ImGui.inputText("Name", nameBuffer)) {
            name = nameBuffer.get();
        }
    }

    private transient imgui.type.ImString nameBuffer;
}
