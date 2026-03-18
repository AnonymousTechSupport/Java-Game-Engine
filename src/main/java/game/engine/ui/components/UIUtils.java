package game.engine.ui.components;

import imgui.ImGui;

public class UIUtils {

    public static void placeRightAligned(Runnable widget) {
        ImGui.sameLine(ImGui.getContentRegionMaxX() - UITheme.PADDING_SM);
        widget.run();
    }
}
