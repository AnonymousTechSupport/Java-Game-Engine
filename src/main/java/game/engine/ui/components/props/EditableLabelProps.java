package game.engine.ui.components.props;

import game.engine.ui.services.EditContext;

public final class EditableLabelProps extends game.engine.ui.components.WidgetProps {
    public final String id;
    public String initialText;
    public final EditContext editContext;

    public EditableLabelProps(String id, String initialText, EditContext editContext) {
        this.id = id;
        this.initialText = initialText;
        this.editContext = editContext;
    }
}
