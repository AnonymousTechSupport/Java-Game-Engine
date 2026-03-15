package game.engine.ui.core;

/**
 * Convenience base class for UI components that need access to UIContext and EntityRegistry.
 */
public abstract class UIComponentWithContext extends UIComponent {
    protected final UIContext uiContext;

    public UIComponentWithContext(UIContext uiContext) {
        super(uiContext.getEntityRegistry());
        this.uiContext = uiContext;
    }
}
