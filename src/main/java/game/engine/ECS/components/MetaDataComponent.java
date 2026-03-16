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
        ImString nameValue = new ImString(name, 256);
        if (ImGui.inputText("Name", nameValue)) {
            name = nameValue.get();
        }
    }
}
