package game.engine;

import game.engine.ECS.components.Component;
import game.engine.ECS.components.ComponentType;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ComponentRegistry {

    private final Map<ComponentType, Supplier<? extends Component>> suppliers = new HashMap<>();

    public <T extends Component> void register(ComponentType type, Supplier<T> supplier) {
        suppliers.put(type, supplier);
    }

    public Component createComponent(ComponentType type) {
        Supplier<? extends Component> supplier = suppliers.get(type);
        if (supplier != null) {
            return supplier.get();
        }
        return null;
    }

    public ComponentType[] getAvailableComponentTypes() {
        return suppliers.keySet().toArray(new ComponentType[0]);
    }
}
