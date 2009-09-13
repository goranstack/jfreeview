package se.bluebrim.view.select;

import java.awt.event.MouseEvent;
import java.util.*;

import se.bluebrim.view.*;
import se.bluebrim.view.View.ViewFilter;

/**
 * Selection manaager that only allows selection of one object at the time
 * 
 * @author G Stack
 *
 */
public class SingleSelectionManager extends AbstractSelectionManager
{
	private Object selectedId;

	public SingleSelectionManager(SelectableLookup selectableLookup)
	{
		super(selectableLookup);
	}

	public void select(Selectable selectable, MouseEvent e)
	{
		if (e.isControlDown() && selectable.getId().equals(selectedId))
			setSelected(null);
		else
			setSelected(selectable);
	}
	
	private void setSelected(Selectable newSelected)
	{
		Selectable oldSelected = getSelected();
		if (newSelected != null)
		{
			selectedId = newSelected.getId();
			newSelected.selectionChanged();
		} else
			selectedId = null;
		if (oldSelected != null)
			oldSelected.selectionChanged();
	}

	/**
	 * Do nothing here
	 */
	public void select(List views, MouseEvent e)
	{

	}

	/**
	 * Do nothing here
	 */
	public void selectAll()
	{

	}

	public boolean isSelected(Selectable view)
	{
		return view.getId().equals(selectedId);
	}

	public List getSelection()
	{
		List list = new ArrayList();
		list.add(selectedId);
		return list;
	}

	/**
	 * TODO: This metod was created when a SelectionManager only handled Views.
	 * Now when also models can be Selectable we should rework this method
	 */
	public List getSelection(ViewFilter filter)
	{
		List list = new ArrayList();
		Selectable selected = getSelected();
		if (selected instanceof View && filter.accept((View)selected))
			list.add(selected);
		return list;
	}

	public void deselectAll()
	{
		setSelected(null);		
	}

	/**
	 * Since there only is one selected this has to be the one
	 */
	public void deselect(Selectable selectable)
	{
		setSelected(null);				
	}
	
	private Selectable getSelected()
	{
		return selectableLookup.getSelectable(selectedId);
	}


}
