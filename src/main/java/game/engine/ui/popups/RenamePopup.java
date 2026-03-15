package game.engine.ui.popups;

import game.engine.ECS.Entity;
import game.engine.EntityRegistry;
import game.engine.ui.UIComponent;
import imgui.ImGui;
import imgui.flag.ImGuiInputTextFlags;
import imgui.type.ImString;

/**
 * A modal popup for renaming an entity.
 */
public class RenamePopup extends UIComponent {
    private static int renameTargetId = -1;
    private static final ImString renameBuffer = new ImString(128);
    private static boolean renameRequested = false;
    private static boolean renameJustOpened = false;

    public RenamePopup(EntityRegistry entityRegistry) {
        super(entityRegistry);
    }

    /**
     * Static method to request the rename popup for a specific entity.
     * @param entityId The ID of the entity to rename.
     * @param registry The EntityRegistry to get the current name from.
     */
    public static void requestRename(int entityId, EntityRegistry registry) {
        renameTargetId = entityId;
        Entity current = registry.getEntity(renameTargetId);
        String curName = (current != null && current.getName() != null) ? current.getName() : ("Entity " + renameTargetId);
        renameBuffer.set(curName);
        renameRequested = true;
    }

    @Override
    public void render() {
        if (renameRequested) {
            ImGui.openPopup("Rename Entity");
            renameRequested = false;
            renameJustOpened = true;
        }

        if (ImGui.beginPopupModal("Rename Entity", imgui.flag.ImGuiWindowFlags.AlwaysAutoResize)) {
            ImGui.text("Rename entity " + renameTargetId);
            ImGui.separator();

            boolean submitted = ImGui.inputText("Name", renameBuffer, ImGuiInputTextFlags.EnterReturnsTrue);
            if (renameJustOpened) {
                ImGui.setKeyboardFocusHere(0);
                renameJustOpened = false;
            }

            if (submitted) {
                handleRename();
                ImGui.closeCurrentPopup();
            }

            if (ImGui.button("OK")) {
                handleRename();
                ImGui.closeCurrentPopup();
            }
            ImGui.sameLine();
            if (ImGui.button("Cancel")) {
                ImGui.closeCurrentPopup();
            }

            ImGui.endPopup();
        }
    }

    private void handleRename() {
        String newName = renameBuffer.get().trim();
        if (!newName.isEmpty() && renameTargetId >= 0) {
            entityRegistry.renameEntity(renameTargetId, newName);
        }
    }
}
