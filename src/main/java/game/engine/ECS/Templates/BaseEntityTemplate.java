package game.engine.ECS.Templates;

import game.engine.EntityRegistry;

public interface BaseEntityTemplate {
    String getName();

    int instantiate(EntityRegistry registry);
}
