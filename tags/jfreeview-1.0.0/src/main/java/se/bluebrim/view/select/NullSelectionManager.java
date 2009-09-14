package se.bluebrim.view.select;

import java.awt.event.MouseEvent;
import java.util.*;

import javax.swing.JComponent;

import se.bluebrim.view.*;
import se.bluebrim.view.View.ViewFilter;

/**
 * Used when no selction should take place
 * 
 * @author G Stack
 *
 */
public class NullSelectionManager implements SelectionManager
{
	public NullSelectionManager()
	{
	}

	public void select(Selectable view, MouseEvent e)
	{
	}

	public void select(List views, MouseEvent e)
	{
	}

	public void selectAll()
	{
	}

	public boolean isSelected(Selectable view)
	{
		return false;
	}

	public void setRootView(ParentView rootView)
	{
	}

	public List getSelection()
	{
		return new ArrayList();
	}

	public List getSelection(ViewFilter filter)
	{
		return getSelection();
	}

	public SelectionManager getCopy()
	{
		return this;
	}

	public void setComponent(JComponent component)
	{
		
	}

	public void deselectAll()
	{
		// TODO Auto-generated method stub
		
	}

	public void deselect(Selectable selectable)
	{
		// TODO Auto-generated method stub
		
	}

}
