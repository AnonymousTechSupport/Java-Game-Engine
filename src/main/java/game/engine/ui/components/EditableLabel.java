package game.engine.ui.components;

import game.engine.ui.EditContext;
import imgui.ImGui;
import imgui.flag.ImGuiInputTextFlags;
import imgui.type.ImString;

/**
 * A reusable inline editable label used across the editor UI. It encapsulates
 * the edit state, buffer, focus handling and provides a simple callback API
 * so callers can react to commit/cancel without duplicating UI logic.
 */
public class EditableLabel {
    private final String id; // stable id used to build internal ImGui widget ids
    private final ImString buffer;
    private boolean editing = false;
    private boolean justOpened = false;
    private final EditContext editContext; // manages single-active-editor policy

    /** Callback interface invoked on commit/cancel events. */
    public interface Callback {
        void onCommit(String newText);
        void onCancel();
    }

    /**
     * Create an editable label.
     * @param id stable identifier (used in internal ImGui ids)
     * @param initialText initial visible text
     */
    public EditableLabel(String id, String initialText, EditContext editContext) {
        this.id = id;
        this.buffer = new ImString(128);
        this.buffer.set(initialText != null ? initialText : "");
        this.editContext = editContext;
    }

    /** Update the visible text (used when external renames occur). */
    public void setText(String text) { buffer.set(text == null ? "" : text); }

    /** Programmatically start editing with an initial buffer. Called directly by callers. */
    public void startEdit(String initial) {
        // Start editing directly without enforcing EditContext rules. This is a
        // convenience method used by panels that have already negotiated editing
        // ownership through EditContext if needed.
        beginEditInternal(initial);
    }

    /** Cancel the current edit and discard the buffer. */
    public void cancel() {
        editing = false;
        justOpened = false;
        // inform EditContext if this was the active editor
        if (editContext != null) editContext.notifyClosed(this);
    }

    /** Returns true while the label is in edit mode. */
    public boolean isEditing() { return editing; }

    /**
     * Begin editing immediately. Called by EditContext when it accepts a request.
     * @param initial initial text to place in the buffer (may be null)
     */
    public void beginEditInternal(String initial) {
        buffer.set(initial == null ? "" : initial);
        editing = true;
        justOpened = true;
    }

    /**
     * Request the edit context to start editing this entity. Returns true if edit started.
     * @param entityId entity this editor represents (used by EditContext for matching)
     */
    public boolean tryStart(int entityId) {
        if (editContext == null) {
            beginEditInternal(null);
            return true;
        }
        return editContext.requestStartEdit(entityId, this, buffer.get());
    }

    /** Request focus for the current editor (called by EditContext). */
    public void requestFocus() {
        justOpened = true; // next render will focus
    }

    /**
     * Render the label. When not editing this shows a selectable label which
     * starts edit on double-click. When editing, shows an input text and Cancel
     * button. Commit and cancel are reported via the callback.
     *
     * @param labelVisible the human-readable label to display
     * @param cb callback invoked on commit/cancel
     * @param onSelect runnable executed on single left-click (for selection)
     */
    public void render(String labelVisible, int entityId, Callback cb, Runnable onSelect) {
        if (!editing) {
            ImGui.selectable(labelVisible);

            // Left-click to select, double-click to edit
            if (ImGui.isItemClicked(0)) {
                if (ImGui.isMouseDoubleClicked(0)) {
                    // tryStart will enforce EditContext rules
                    tryStart(entityId);
                } else if (onSelect != null) {
                    onSelect.run(); // notify panel of selection
                }
            }
        } else {
            // Internal widget id uses ## suffix to avoid visible-label collisions
            String widgetId = "##editable-" + id;
            boolean submitted = ImGui.inputText(widgetId, buffer, ImGuiInputTextFlags.EnterReturnsTrue);
            if (justOpened) {
                // Focus the input once when editing starts
                ImGui.setKeyboardFocusHere(0);
                justOpened = false;
            }
            ImGui.sameLine();
            if (ImGui.button("Cancel##cancel-" + id)) {
                editing = false;
                cb.onCancel();
                if (editContext != null) editContext.notifyClosed(this);
                return;
            }
            if (submitted) {
                editing = false;
                cb.onCommit(buffer.get().trim());
                if (editContext != null) editContext.notifyClosed(this);
            }
        }
    }
}
