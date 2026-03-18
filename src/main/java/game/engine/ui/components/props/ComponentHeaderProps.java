package game.engine.ui.components.props;

import game.engine.ui.components.InlineActionBar;
import game.engine.ui.components.Widget;
import java.util.function.Consumer;

public final class ComponentHeaderProps extends game.engine.ui.components.WidgetProps {
    public final String title;
    public final boolean initiallyOpen;
    public final Widget content;
    public final InlineActionBar actionBar;
    public final Consumer<game.engine.ui.components.ContextMenu.MenuBuilder> contextMenuBuilder;

    public ComponentHeaderProps(String title, boolean initiallyOpen, Widget content, InlineActionBar actionBar, Consumer<game.engine.ui.components.ContextMenu.MenuBuilder> contextMenuBuilder) {
        this.title = title;
        this.initiallyOpen = initiallyOpen;
        this.content = content;
        this.actionBar = actionBar;
        this.contextMenuBuilder = contextMenuBuilder;
    }
}
