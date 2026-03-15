package game.engine.ui.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A centralized service that manages the currently selected entity. It provides
 * listener callbacks so that any UI panel or system can react to selection
 * changes without direct coupling.
 *
 * This service enforces a single-selection policy. It also suppresses
 * notifications if a selection event does not change the current selection,
 * preventing redundant UI updates.
 */
public class SelectionService {

    /**
     * Listener interface for components that need to react to selection changes.
     */
    public interface SelectionListener {
        /**
         * Called when the selected entity changes.
         * @param newSelection the newly selected entity ID, or -1 if none.
         */
        void onSelectionChanged(Selection newSelection);
    }

    private Selection selected = null;
    private final List<SelectionListener> listeners = new ArrayList<>();

    public Selection getSelected() {
        return this.selected;
    }

    public void setSelected(Selection selection) {
        if (Objects.equals(this.selected, selection)) {
            return; // No change, suppress notification
        }
        this.selected = selection;
        notifyListeners();
    }


    /**
     * Clears the current selection (sets entity ID to -1) and notifies listeners
     * if the selection was not already empty.
     */
    public void clear() {
        if (this.selected == null) {
            return; // No change
        }
        this.selected = null;
        notifyListeners();
    }

    /**
     * Registers a listener to be notified of selection changes.
     * @param listener The listener to add. Must not be null.
     */
    public void addListener(SelectionListener listener) {
        Objects.requireNonNull(listener, "SelectionListener cannot be null");
        listeners.add(listener);
    }

    /**
     * Unregisters a listener.
     * @param listener The listener to remove.
     */
    public void removeListener(SelectionListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (SelectionListener listener : listeners) {
            try {
                listener.onSelectionChanged(this.selected);
            } catch (Exception e) {
                // Use project logger to record listener errors. We choose ENGINE
                // category for now; creating a dedicated UI category could be
                // implemented if needed.
                game.engine.logging.Logger.error(game.engine.logging.Logger.UI,
                        "Error in selection listener: " + e.getMessage(), e);
            }
        }
    }
}
