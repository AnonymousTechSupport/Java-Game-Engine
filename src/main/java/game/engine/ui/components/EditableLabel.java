package game.engine.ui.components;

import game.engine.ui.services.EditContext;
import imgui.flag.ImGuiKey;
import imgui.ImGui;
import imgui.flag.ImGuiInputTextFlags;
import imgui.type.ImString;

/**
 * A reusable inline editable label used across the editor UI. It encapsulates
 * the edit state, buffer, focus handling and provides a simple callback API
 * so callers can react to commit/cancel without duplicating UI logic.
 */
public class EditableLabel implements InlineEditor {
    private final String id; // stable id for ImGui widgets
    private final ImString buffer;
    private boolean editing = false;
    private boolean justOpened = false;
    private final EditContext editContext; // enforces single active editor

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

    /**
     * Programmatically start editing with an initial buffer. Called directly by callers.
     */
    public void startEdit(String initial) { beginEditInternal(initial); }

    /** Cancel the current edit and discard the buffer. */
    public void cancel() {
        editing = false;
        justOpened = false;
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
    public void requestFocus() { justOpened = true; }

    /**
     * Render the label inside a row of known height. Caller must supply
     * the target rowHeight so the label can be vertically centered.
     *
     * @param labelVisible the human-readable label to display
     * @param entityId the entity id this label represents
     * @param selected whether the row should be drawn as selected
     * @param cb callback invoked on commit/cancel
     * @param onSelect runnable executed on single left-click (for selection)
     * @param rowHeight exact height (pixels) to use for the row
     */
    public void render(String labelVisible, int entityId, boolean selected, Callback cb, Runnable onSelect, float rowHeight) {
        if (!editing) {
            float frameHeight = ImGui.getFrameHeight();
            float centerOffset = (rowHeight - frameHeight) * 0.5f;
            if (centerOffset > 0) {
                ImGui.setCursorPosY(ImGui.getCursorPosY() + centerOffset);
            }

            ImGui.selectable(labelVisible + "##sel-" + id, selected, 0, 0, rowHeight);

            boolean hovered = ImGui.isItemHovered();
            boolean mouseDouble = ImGui.isMouseDoubleClicked(0);
            boolean mouseClicked = ImGui.isItemClicked(0);

            if (mouseDouble && hovered) {
                boolean started = tryStart(entityId);
                if (started && onSelect != null) onSelect.run();
            } else if (mouseClicked && hovered) {
                if (onSelect != null) onSelect.run();
            }
        } else {
            float frameHeight = ImGui.getFrameHeight();
            float centerOffset = (rowHeight - frameHeight) * 0.5f;
            if (centerOffset > 0) {
                ImGui.setCursorPosY(ImGui.getCursorPosY() + centerOffset);
            }

            ImGui.setNextItemWidth(ImGui.getContentRegionAvailX());
            String widgetId = "##editable-" + id;
            boolean submitted = ImGui.inputText(widgetId, buffer, ImGuiInputTextFlags.EnterReturnsTrue);

            if (justOpened) {
                ImGui.setKeyboardFocusHere(0);
                justOpened = false;
            }

            int escKey = ImGui.getIO().getKeyMap(ImGuiKey.Escape);
            if (ImGui.isKeyPressed(escKey)) {
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