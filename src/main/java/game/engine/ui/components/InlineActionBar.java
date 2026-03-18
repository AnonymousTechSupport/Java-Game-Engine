package game.engine.ui.components;

import game.engine.ui.core.UIContext;
import imgui.ImGui;

import java.util.ArrayList;
import java.util.List;

public class InlineActionBar implements Widget {

    private final List<Widget> actions = new ArrayList<>();

    public InlineActionBar addAction(Widget action) {
        actions.add(action);
        return this;
    }

    @Override
    public void render(UIContext context) {
        // Estimate width for inline actions and align them to the right side of
        // the content region so they don't overlap the selectable/label area.
        int count = actions.size();
        if (count == 0)
            return;
        float estimatedWidth = count * 26f; // approx per-button width
        float targetX = ImGui.getContentRegionMaxX() - estimatedWidth;
        ImGui.sameLine(targetX);
        for (int i = 0; i < actions.size(); i++) {
            Widget action = actions.get(i);
            action.render(context);
            if (i < actions.size() - 1)
                ImGui.sameLine();
        }
    }
}
