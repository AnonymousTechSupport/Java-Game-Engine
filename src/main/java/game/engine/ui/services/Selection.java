package game.engine.ui.services;

import game.engine.ECS.components.ComponentType;

public class Selection {
    private final int entityId;
    private final ComponentType componentType;

    public Selection(int entityId) {
        this.entityId = entityId;
        this.componentType = null;
    }

    public Selection(int entityId, ComponentType componentType) {
        this.entityId = entityId;
        this.componentType = componentType;
    }

    public int getEntityId() {
        return entityId;
    }

    public ComponentType getComponentType() {
        return componentType;
    }

    public boolean isEntity() {
        return componentType == null;
    }

    public boolean isComponent() {
        return componentType != null;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Selection selection = (Selection) obj;
        return entityId == selection.entityId && componentType == selection.componentType;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(entityId, componentType);
    }
}
