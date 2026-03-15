package game.engine.ui.components;

/**
 * A small interface describing the behavior required by the EditContext for
 * inline editors. Implementations may be text inputs, dropdowns, or other
 * widgets which allow inline editing of a property.
 */
public interface InlineEditor {
    /** Begin editing with the provided initial text. Called by EditContext. */
    void beginEditInternal(String initialText);

    /** Cancel the current edit and revert any temporary state. */
    void cancel();

    /** Request keyboard focus for the editor. */
    void requestFocus();

    /** Return true if the editor is currently active. */
    boolean isEditing();
}
