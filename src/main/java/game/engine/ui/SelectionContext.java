package game.engine.ui;

/** Simple holder for the currently selected entity id (-1 == none). */
public class SelectionContext {
    private int selectedEntityId = -1;
    public int getSelectedEntityId() { return selectedEntityId; }
    public void setSelectedEntityId(int id) { this.selectedEntityId = id; }
    public void clear() { this.selectedEntityId = -1; }
}
