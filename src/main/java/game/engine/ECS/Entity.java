package game.engine.ECS;

/**
 * Lightweight entity model exposing id and name for UI and tooling.
 */
public final class Entity {
    private final int id;
    private final String name;

    public Entity(int id, String name) {
        this.id = id;
        this.name = name;
    }

    /** Returns the integer entity id. */
    public int getId() { return id; }

    /** Returns the entity's display name. */
    public String getName() { return name; }
}
