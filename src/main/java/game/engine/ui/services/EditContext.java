package game.engine.ui.services;

import game.engine.ui.components.InlineEditor;

/**
 * Tracks the single active inline editor in the UI. Enforces a
 * single-active-editor rule: attempts to start a new edit will fail while
 * another editor is active. The current editor can be focused so the user is
 * guided to finish or cancel it.
 *
 * This class depends on the InlineEditor interface so it is decoupled from any
 * concrete editor implementation (such as EditableLabel).
 */
public class EditContext {
    private int currentEntityId = -1;
    private InlineEditor currentEditor = null;

    /**
     * Request to start editing for the given entity using the provided editor
     * instance. If no editor is active this will activate the provided editor
     * and return true. If another editor is active, this returns false and
     * focuses the active editor. This method must be called from the UI thread.
     */
    public boolean requestStartEdit(int entityId, InlineEditor editor, String initial) {
        if (currentEditor == null) {
            currentEditor = editor;
            currentEntityId = entityId;
            // instruct the editor to begin edit (sets focus next frame)
            editor.beginEditInternal(initial);
            return true;
        }
        // if same editor and same entity, allow (idempotent)
        if (currentEditor == editor && currentEntityId == entityId)
            return true;

        // otherwise focus existing editor so user completes it first
        focusCurrentEditor();
        return false;
    }

    /**
     * Notify the context that the given editor has closed (commit or cancel).
     */
    public void notifyClosed(InlineEditor editor) {
        if (currentEditor == editor) {
            currentEditor = null;
            currentEntityId = -1;
        }
    }

    /** If the supplied entity id is currently being edited, cancel it. */
    public void cancelIfMatch(int entityId) {
        if (currentEntityId == entityId && currentEditor != null) {
            currentEditor.cancel();
            // notifyClosed will clear the rest
        }
    }

    /** Cancel any active edit. */
    public void cancel() {
        if (currentEditor != null) {
            currentEditor.cancel();
        }
    }

    /** Return true if any editor is currently active. */
    public boolean isAnyEditing() {
        return currentEditor != null;
    }

    /** Return true if the specified entity is the one being edited. */
    public boolean isEditing(int entityId) {
        return currentEntityId == entityId && currentEditor != null;
    }

    /** Focus the currently active editor so the user can finish it. */
    public void focusCurrentEditor() {
        if (currentEditor != null)
            currentEditor.requestFocus();
    }
}
