package se.bluebrim.view.select;

import java.awt.event.MouseEvent;
import java.util.List;

import se.bluebrim.view.*;

/**
 * Implemented by objects that keep track of selection. Implementing objects
 * can implement various selection principles for example singel or
 * multiple selection. A SelectionManager reference the selected object
 * through id to handle cases where the selected objects are views that are
 * replaced with new instances when updated.
 * 
 * @author G Stack
 *
 */
public interface SelectionManager
{
	
	public abstract void select(Selectable view, MouseEvent e);

	public abstract void select(List views, MouseEvent e);

	public abstract void selectAll();
	
	public abstract void deselectAll();

	public abstract boolean isSelected(Selectable view);
	
	public void setRootView(ParentView rootView);
	
	public List getSelection();
	
	public List getSelection(View.ViewFilter filter);

	public abstract SelectionManager getCopy();
	
	public abstract void deselect(Selectable selectable);

}