package game.engine.ui.services;

import game.engine.EntityRegistry;

/**
 * Centralized rename applier. This service contains the single place that validates
 * and applies entity renames so UI components (inline editors) don't duplicate logic.
 */
public class RenameService {
    private final EntityRegistry registry;

    public RenameService(EntityRegistry registry) {
        this.registry = registry;
    }

    /**
     * Validate and apply a rename for the given entity id. Empty or whitespace-only
     * names are ignored. This method is intended to be called from UI code on the
     * main thread.
     *
     * @param entityId id of the entity to rename
     * @param newName  the proposed new name
     */
    public void commitRename(int entityId, String newName) {
        String trimmed = newName == null ? "" : newName.trim();
        if (trimmed.isEmpty()) return;
        // Ensure entity still exists before applying
        if (registry.getEntity(entityId) == null) return;
        registry.renameEntity(entityId, trimmed);
    }
}
