package game.engine.ui.components;

import imgui.ImGui;
import imgui.type.ImInt;

/**
 * Static helpers for dropdowns/combo boxes used from inspectors (no UIContext).
 */
public final class Dropdown {
    private Dropdown() {
    }

    public static boolean renderCombo(String label, ImInt index, String[] items) {
        return ImGui.combo(label, index, items, items.length);
    }

    public static int renderCombo(String label, int index, String[] items) {
        // Use a transient buffer to preserve selection edits while user
        // interacts
        imgui.type.ImInt buffer = new imgui.type.ImInt(index);
        boolean changed = ImGui.combo(label, buffer, items, items.length);
        return changed ? buffer.get() : index;
    }
}
