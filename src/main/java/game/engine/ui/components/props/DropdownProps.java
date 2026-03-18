package game.engine.ui.components.props;

import java.util.function.Consumer;

public final class DropdownProps extends game.engine.ui.components.WidgetProps {
    public final String label;
    public final String[] options;
    public int selectedIndex;
    public final Consumer<Integer> onChange;
    public final boolean showLabel;

    public DropdownProps(String label, String[] options, int selectedIndex, Consumer<Integer> onChange, boolean showLabel) {
        this.label = label;
        this.options = options;
        this.selectedIndex = selectedIndex;
        this.onChange = onChange;
        this.showLabel = showLabel;
    }
}
