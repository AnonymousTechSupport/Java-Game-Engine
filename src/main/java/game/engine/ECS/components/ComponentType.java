package game.engine.ECS.components;

public enum ComponentType {
    TRANSFORM(Category.ENGINE, TransformComponent.class), RENDER(Category.ENGINE, RenderComponent.class),
    CAMERA(Category.ENGINE, CameraComponent.class), METADATA(Category.INTERNAL, MetaDataComponent.class);

    /**
     * The category of a component type, used for grouping and filtering in the
     * UI.
     */
    public enum Category {
        ENGINE, CUSTOM, INTERNAL
    }

    private final Category category;
    private final Class<? extends Component> componentClass;

    /**
     * Constructs a ComponentType with the specified category and component
     * class.
     * 
     * @param category the category of the component type, used for grouping and
     * filtering in the UI
     * @param componentClass the class of the component associated with this
     * type, used for instantiation and type information
     */
    ComponentType(Category category, Class<? extends Component> componentClass) {
        this.category = category;
        this.componentClass = componentClass;
    }

    /**
     * Returns the category of this component type, which can be used to group
     * components in the UI and for filtering.
     * 
     * @return the category of this component type, which can be used to group
     * components in the UI and for filtering
     */
    public Category getCategory() {
        return category;
    }

    /**
     * Returns the class of the component associated with this type.
     * 
     * @return the class of the component associated with this type
     */
    public Class<? extends Component> getComponentClass() {
        return componentClass;
    }
}
