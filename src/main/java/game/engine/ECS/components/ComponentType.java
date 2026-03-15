package game.engine.ECS.components;

public enum ComponentType {
    TRANSFORM(Category.ENGINE),
    RECTANGLE(Category.ENGINE),
    BALL(Category.ENGINE);

    public enum Category {
        ENGINE,
        CUSTOM
    }

    private final Category category;

    ComponentType(Category category) {
        this.category = category;
    }

    public Category getCategory() {
        return category;
    }
}
