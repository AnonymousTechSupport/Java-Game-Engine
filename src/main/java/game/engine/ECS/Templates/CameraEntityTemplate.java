package game.engine.ECS.Templates;

import game.engine.EntityRegistry;
import game.engine.ECS.components.ComponentType;
import game.engine.ECS.components.TransformComponent;
import game.engine.ECS.components.CameraComponent;
import game.engine.ECS.Entity;

/**
 * Template to create a camera entity with Transform and Camera components.
 */
public class CameraEntityTemplate implements BaseEntityTemplate {
    @Override
    public String getName() {
        return "Camera";
    }

    @Override
    public int instantiate(EntityRegistry registry) {
        Entity entity = registry.createEntity("Camera");
        int id = entity.getId();

        registry.addComponent(id, ComponentType.TRANSFORM);
        registry.addComponent(id, ComponentType.CAMERA);

        // Initialize components with sensible defaults
        TransformComponent tc = (TransformComponent) registry.getWorld().getComponent(id, ComponentType.TRANSFORM);
        if (tc != null) {
            tc.position.set(0f, 0f);
            tc.scale.set(1f, 1f);
        }

        CameraComponent cam = (CameraComponent) registry.getWorld().getComponent(id, ComponentType.CAMERA);
        if (cam != null) {
            cam.position.set(0f, 0f);
            cam.rotation = 0f;
            cam.zoom = 1f;
        }

        // If there is no main camera yet, set this one as default
        game.engine.World world = registry.getWorld();
        if (world.getMainCameraEntityId() == -1) {
            world.setMainCameraEntityId(id);
        }

        return id;
    }
}
