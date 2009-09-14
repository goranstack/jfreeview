package se.bluebrim.view.select;

import se.bluebrim.view.Selectable;

/**
 * Implemented by objects that can look up Selectable's for a SelectionManager.
 * Uses the equals-method when looking up the Selectable.
 * 
 * @author G Stack
 *
 */
public interface SelectableLookup
{
	public Selectable getSelectable(Object id);
}
