package se.bluebrim.view;

import java.awt.event.MouseEvent;

/**
 * Implemented by views that is managered by a <code>SelectionManager</code>
 * and manipulated by <code>SelectionTool</code>. The selection mechanism
 * do not require a selection state in the Selectable. A Selectable is selected
 * by being included in the SelectionManager selected set.
 * 
 * @author G Stack
 */
public interface Selectable
{
	public boolean isSelected();
	public void select(MouseEvent mouseEvent);
	public void selectionChanged();
	public Object getId();
}
