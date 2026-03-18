package game.engine.ui.components.props;

import game.engine.ui.core.UIContext;
import java.util.function.Consumer;

public final class ButtonProps extends game.engine.ui.components.WidgetProps {
    public final String label;
    public final Consumer<UIContext> onClick;
    public final float width;
    public final String tooltip;

    public ButtonProps(String label, Consumer<UIContext> onClick, float width, String tooltip) {
        this.label = label;
        this.onClick = onClick;
        this.width = width;
        this.tooltip = tooltip;
    }
}
