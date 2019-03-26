package se.bluebrim.view.select;

import java.awt.event.MouseEvent;
import java.util.*;

import se.bluebrim.view.*;

/**
 * The StandardSelectionManager implements the standard selection behavior that has
 * become a defacto standard in GUI that displays some kind of elements. The behaviour is characterized of:
 * <ul>
 * <li>A selection of one or more elements deselects present selection if no modifier key is pressed.</li>
 * <li>Shift-select extends present selection with an intervall of elements from last 
 * element of present selection to the selected element.</li>
 * <li>Control select toggles selection of the selected element. Selected element are added to previous selection</li>
 * </ul>
 * The StandardSelectionManager also handles the repaint of views that has changed selection state.<br>
 * 
 * @author G Stack
 */
public class StandardSelectionManager extends AbstractSelectionManager
{
	private List selectedIds;

	public StandardSelectionManager(SelectableLookup selectableLookup)
	{
		super(selectableLookup);
		selectedIds = new ArrayList();
	}
	
	public void select(Selectable view, MouseEvent e)
	{
		if (e.isShiftDown())
			if (selectedIds.isEmpty())
				select(view);
			else
				selectInterval(view);
		else if (e.isControlDown())
			toggleSelect(view);
		else {
			deselectAll();
			select(view);
		}
	}

	/**
	 * Select all views from specified view to previous selected in the
	 * all views list. The same behaviour can be found in Windows explorer
	 * in case you want to verify the behaviour. <br>
	 * The method assume that selected views not are empty.
	 */
	private void selectInterval(Selectable endView)
	{
		List allViews = getAllViews();
		int selectedIndex = allViews.indexOf(endView);
		int firstSelected = Integer.MAX_VALUE;
		int lastSelected = -1;

		for (Iterator iter = selectedIds.iterator(); iter.hasNext();) 
		{
			Selectable view = getSelectable(iter.next());
			int index = allViews.indexOf(view);
			firstSelected = Math.min(firstSelected, index);
			lastSelected = Math.max(lastSelected, index);
		}
		
		int start;
		int end;
		
		if (selectedIndex <= firstSelected)
		{
			start = selectedIndex;
			end = lastSelected;
		} else
			if (selectedIndex >= lastSelected)
			{
				start = firstSelected;
				end = selectedIndex;
			} else
			{
				start = selectedIndex;
				end = lastSelected;
			}
		
		for (int i = start; i <= end; i++) 
		{		
			Object view = allViews.get(i);
			if (view instanceof Selectable)
				select((Selectable)view);
		}
	}
	
	private List getAllViews()
	{
		return rootView.getAllDescendents();
	}

	
	public void select(List views, MouseEvent e)
	{
		if (!e.isShiftDown() && !e.isControlDown())
			deselectAll();
		for (Iterator iter = views.iterator(); iter.hasNext();) 
		{
			Object view = iter.next();
			if (view instanceof Selectable)
				select((Selectable)view);
		}							
	}
	
	public void deselectAll()
	{
		List previousSelected = new ArrayList(selectedIds);
		selectedIds = new ArrayList();
		for (Iterator iter = previousSelected.iterator(); iter.hasNext();) 
			getSelectable(iter.next()).selectionChanged();

	}
	
	private Selectable getSelectable(Object id)
	{
		return selectableLookup.getSelectable(id);
	}
	
	public void selectAll()
	{
		for (Iterator iter = getAllViews().iterator(); iter.hasNext();) 
		{
			Selectable view = (Selectable) iter.next();
			if (!view.isSelected()) 
			{
				addSelected(view);
			}
		}
	}

	private void addSelected(Selectable view)
	{
		selectedIds.add(view.getId());
		view.selectionChanged();
	}

	/**
	 * Repaint the dirty region before and after change
	 * of the selection state in case dirty region changes which is the case 
	 * when selection is marked with a thicker frame.
	 */
	private void select(Selectable selectable)
	{
		if (!selectable.isSelected())
		{
			addSelected(selectable);
		}
	}

	/**
	 * Repaint the dirty region before and after change
	 * of the selection state in case dirty region changes which is the case 
	 * when selection is marked with a thicker frame.
	 */	
	public void deselect(Selectable selectable)
	{
		if (selectable.isSelected())
		{
			selectedIds.remove(selectable.getId());
			selectable.selectionChanged();
		}
	}
	
	private void toggleSelect(Selectable view)
	{
		if (view.isSelected())
			deselect(view);
		else
			select(view);
	}
		
	public boolean isSelected(Selectable view)
	{
		return selectedIds.contains(view.getId());
	}

	public List getSelection()
	{
		return selectedIds;
	}
	
	/**
	 * TODO: This metod was created when a SelectionManager only handled Views.
	 * Now when also models can be Selectable we should rework this method
	 */
	public List getSelection(View.ViewFilter filter)
	{
		List filtered = new ArrayList();
		for (Iterator iter = selectedIds.iterator(); iter.hasNext();) 
		{
			
			Selectable selected = getSelectable(iter.next());
			if (selected instanceof View && filter.accept((View)selected))
				filtered.add(selected);
		}
		return filtered;
	}
	
	public SelectionManager getCopy()
	{
		StandardSelectionManager copy = (StandardSelectionManager)super.getCopy();
		copy.selectedIds = new ArrayList();
		return copy;
	}

	

}
