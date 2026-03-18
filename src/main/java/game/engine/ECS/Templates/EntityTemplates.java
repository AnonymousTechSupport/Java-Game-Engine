package game.engine.ECS.Templates;

import java.util.Collections;
import java.util.List;
import java.util.Iterator;
import game.engine.EntityRegistry;
import game.engine.ECS.components.*;

import game.engine.ECS.Entity;

public class EntityTemplates implements Iterable<BaseEntityTemplate> {
    private static final List<BaseEntityTemplate> TEMPLATES = List.of(
            new BaseEntityTemplate() {
                @Override
                public String getName() {
                    return "Empty";
                }

                @Override
                public int instantiate(EntityRegistry registry) {
                    Entity entity = registry.createEntity("Empty");
                    int id = entity.getId();
                    registry.addComponent(id, ComponentType.TRANSFORM);
                    return id;
                }
            },
            new CameraEntityTemplate());

    // Single shared instance — templates are static and immutable for now.
    private static final EntityTemplates INSTANCE = new EntityTemplates();

    public static EntityTemplates getInstance() {
        return INSTANCE;
    }

    @Override
    public Iterator<BaseEntityTemplate> iterator() {
        return Collections.unmodifiableList(TEMPLATES).iterator();
    }
}
