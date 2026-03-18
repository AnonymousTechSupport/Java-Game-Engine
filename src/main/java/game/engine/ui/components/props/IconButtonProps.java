package game.engine.ui.components.props;

import game.engine.ui.core.UIContext;
import java.util.function.Consumer;

public final class IconButtonProps extends game.engine.ui.components.WidgetProps {
    public final String icon;
    public final Consumer<UIContext> onClick;

    public IconButtonProps(String icon, Consumer<UIContext> onClick) {
        this.icon = icon;
        this.onClick = onClick;
    }
}
